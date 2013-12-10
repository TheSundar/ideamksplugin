package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.vcsUtil.VcsUtil;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MKSHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Thibaut Fagart
 */
public class SandboxCacheImpl implements SandboxCache {

	private final Logger LOGGER = Logger.getInstance(getClass().getName());
	private final Object lock = new Object();
	/**
	 * sandboxFolder virtualFile => TriclopsSiSandbox
	 */
	private final HashMap<VirtualFile, MksSandboxInfo> sandboxByFolder = new HashMap<VirtualFile, MksSandboxInfo>();
	private final HashSet<VirtualFile> sandboxVFiles = new HashSet<VirtualFile>();
	private final HashSet<MksSandboxInfo> outOfScopeSandboxes = new HashSet<MksSandboxInfo>();
	@NotNull
	private final Project project;
	/**
	 * keeps those paths that were rejected because IDEA directoryIndex is not
	 * intialized
	 */
	private final ArrayList<MksSandboxInfo> pendingUpdates = new ArrayList<MksSandboxInfo>();

	final Thread backgroundUpdates = new Thread(new Runnable() {
		static final long SLEEP_TIME = 5000;

		public void run() {
			while (!stopBackgroundThread) {
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				ArrayList<MksSandboxInfo> tempList;
				synchronized (pendingUpdates) {
					tempList = new ArrayList<MksSandboxInfo>(pendingUpdates.size());
					tempList.addAll(pendingUpdates);
					pendingUpdates.clear();
				}
				for (MksSandboxInfo sandbox : tempList) {
					LOGGER.debug("re-adding" + sandbox);
					addSandbox(sandbox);
				}
			}
		}
	}, "MKS sandbox synchronizer retrier");
	private boolean stopBackgroundThread = false;

	public SandboxCacheImpl(final Project project) {
		this.project = project;
		backgroundUpdates.setDaemon(true);
		backgroundUpdates.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		backgroundUpdates.start();
	}


	public boolean isSandboxProject(@NotNull VirtualFile virtualFile) {
		return sandboxVFiles.contains(virtualFile) || virtualFile.getName().equals("project.pj");
	}

	// todo semble pas renvoyer la bonne sandbox
	@Nullable
	public TriclopsSiSandbox findSandbox(@NotNull VirtualFile virtualFile) {
		MksSandboxInfo sandboxInfo = getSandboxInfo(virtualFile);
		return (sandboxInfo == null) ? null : sandboxInfo.siSandbox;
	}

	public void addSandboxPath(@NotNull String sandboxPath, @NotNull final String mksHostAndPort, @NotNull String mksProject, @NotNull String devPath) {

		VirtualFile sandboxVFile = VcsUtil.getVirtualFile(sandboxPath);
		MksSandboxInfo sandboxInfo = new MksSandboxInfo(sandboxPath, mksHostAndPort, mksProject, devPath, sandboxVFile);
		if (doesSandboxIntersectProject(new File(sandboxPath))) {
			addSandbox(sandboxInfo);
		} else {
			outOfScopeSandboxes.add(sandboxInfo);
			LOGGER.info("ignoring out of project sandbox [" + sandboxPath + "]");
		}

	}

	/**
	 * Should only be called for sandbox relevant for the project (eg : sandbox content and project content intersect)
	 *
	 * @param sandboxInfo
	 */
	private void addSandbox(@NotNull MksSandboxInfo sandboxInfo) {
		String sandboxPath;
		VirtualFile sandboxVFile;
		sandboxPath = sandboxInfo.sandboxPath;
		sandboxVFile = sandboxInfo.sandboxPjFile == null ? VcsUtil.getVirtualFile(sandboxInfo.sandboxPath) : sandboxInfo.sandboxPjFile;
		VirtualFile sandboxFolder;
		if (sandboxVFile != null) {
			sandboxFolder = (sandboxVFile.isDirectory()) ? sandboxVFile : sandboxVFile.getParent();
			if (sandboxFolder == null) {
				LOGGER.warn("unable to find parent VirtualFile for sandbox [" + sandboxVFile + "]");
			}
			synchronized (lock) {
				// ok sandbox in project path
				try {
					sandboxInfo.siSandbox = MKSHelper.createSandbox(sandboxPath);
					sandboxByFolder.put(sandboxFolder, sandboxInfo);
					sandboxVFiles.add(sandboxVFile);
					LOGGER.debug("updated sandbox in cache : " + sandboxVFile);
				} catch (TriclopsException e) {
					LOGGER.error("invalid sandbox ? (" + sandboxPath + ")", e);
					addRejected(sandboxInfo);
				}
			}
		} else {
			LOGGER.warn("unable to find the virtualFile for " + sandboxPath);
			VirtualFile sandboxPjFile = VcsUtil.getVirtualFile(sandboxPath);
			if (sandboxPjFile != null) {
				sandboxInfo = new MksSandboxInfo(sandboxInfo.sandboxPath, sandboxInfo.hostAndPort, sandboxInfo.mksProject,
						sandboxInfo.devPath, sandboxPjFile);
			}
			addRejected(sandboxInfo);
		}
	}

	/**
	 * determines if the sandbox is either UNDER the project or above it, eg if the files controlled by the sandbox
	 * should be monitored
	 *
	 * @param sandboxFile
	 * @return
	 */
	private boolean doesSandboxIntersectProject(@NotNull File sandboxFile) {
		File sandboxFolder = sandboxFile.getParentFile();
		GlobalSearchScope projectScope = project.getProjectScope();
		VirtualFile sandboxVFile = VcsUtil.getVirtualFile(sandboxFile);
		boolean sandboxRelevant = (sandboxVFile != null) && projectScope.contains(sandboxVFile);
		if (!sandboxRelevant) {
			VirtualFile[] projectContentRoots = ProjectRootManager.getInstance(project).getContentRoots();
			for (int i = 0; i < projectContentRoots.length && !sandboxRelevant; i++) {
				VirtualFile projectContentRoot = projectContentRoots[i];
				if (VfsUtil.isAncestor(sandboxFolder, VfsUtil.virtualToIoFile(projectContentRoot), true)) {
					LOGGER.debug("sandbox [" + sandboxFolder + "] contains contentRoot [" + projectContentRoot + "]");
					sandboxRelevant = true;
				}
			}
		}
		return sandboxRelevant;
	}

	private void addRejected(final MksSandboxInfo sandbox) {
		synchronized (pendingUpdates) {
			pendingUpdates.add(sandbox);
		}
	}

	public void clear() {
		synchronized (lock) {
			sandboxByFolder.clear();
			sandboxVFiles.clear();
			outOfScopeSandboxes.clear();
		}
	}

	// for mks monitoring
	public void dumpStateOn(@NotNull PrintWriter pw) {
		pw.println("in project sandboxes");
		List<VirtualFile> sortList = new ArrayList<VirtualFile>(sandboxVFiles);
		Comparator<VirtualFile> comparator = new Comparator<VirtualFile>() {
			public int compare(final VirtualFile virtualFile, final VirtualFile other) {
				if (virtualFile == null) {
					return -1;
				} else if (other == null) {
					return 1;
				} else {
					return virtualFile.toString().compareTo(other.toString());
				}
			}
		};
		Collections.sort(sortList, comparator);
		for (VirtualFile virtualFile : sortList) {
			pw.println(virtualFile);
		}
		sortList.clear();
		for (MksSandboxInfo sandbox : outOfScopeSandboxes) {
			sortList.add(sandbox.sandboxPjFile);
		}
		Collections.sort(sortList, comparator);

		pw.println("OUT OF project sandboxes");
		for (VirtualFile virtualFile : sortList) {
			pw.println(virtualFile);
		}
	}

	public void beforeRootsChange(final ModuleRootEvent event) {
	}

	public void rootsChanged(final ModuleRootEvent event) {
		LOGGER.info("rootsChanged, re computing in/out of project sandboxes");
		synchronized (lock) {
			List<MksSandboxInfo> allSandboxes = new ArrayList<MksSandboxInfo>(sandboxVFiles.size() + outOfScopeSandboxes.size());
			allSandboxes.addAll(sandboxByFolder.values());
			allSandboxes.addAll(outOfScopeSandboxes);
			clear();
			for (MksSandboxInfo sandbox : allSandboxes) {
				addSandbox(sandbox);
			}
		}
	}

	@NotNull
	public Set<MksSandboxInfo> getSandboxesIntersecting(@NotNull final VirtualFile directory) {
		Set<MksSandboxInfo> result = new HashSet<MksSandboxInfo>();
		for (MksSandboxInfo sandboxInfo : sandboxByFolder.values()) {
			VirtualFile sandboxFile = sandboxInfo.sandboxPjFile;
			if (VfsUtil.isAncestor(directory, sandboxFile, false)) {
				result.add(sandboxInfo);
			} else {
				final VirtualFile sandboxParentFile = sandboxFile.getParent();
				if (sandboxParentFile != null && VfsUtil.isAncestor(sandboxParentFile, directory, false)) {
					result.add(sandboxInfo);
				}
			}
		}
		return result;
	}

	public boolean isPartOfSandbox(@NotNull final VirtualFile file) {
		TriclopsSiSandbox sandbox = findSandbox(file);
		TriclopsSiMembers members = MKSHelper.createMembers(sandbox);
		TriclopsSiMember triclopsSiMember = new TriclopsSiMember(file.getPath());
		members.addMember(triclopsSiMember);
		try {
			MKSHelper.getMembersStatus(members);
		} catch (TriclopsException e) {
			LOGGER.error("can't get MKS status for [" + file.getPath() + "]\n" + MKSHelper.getMksErrorMessage(), e);
			return false;
		}
		TriclopsSiMember returnedMember = members.getMember(0);
		return returnedMember.isStatusControlled();
	}

	@Nullable
	public MksSandboxInfo getSandboxInfo(@NotNull final VirtualFile virtualFile) {
		MksSandboxInfo sandbox = null;
		VirtualFile cursorDir = (virtualFile.isDirectory() ? virtualFile : virtualFile.getParent());
		for (; cursorDir != null && sandbox == null; cursorDir = cursorDir.getParent()) {
			sandbox = sandboxByFolder.get(cursorDir);
		}
		return sandbox;
	}

	public void release() {
		stopBackgroundThread = true;
		this.outOfScopeSandboxes.clear();
		this.pendingUpdates.clear();
		this.sandboxByFolder.clear();
		this.sandboxVFiles.clear();
	}
}