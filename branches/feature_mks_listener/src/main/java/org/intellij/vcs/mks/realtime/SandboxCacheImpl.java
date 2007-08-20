package org.intellij.vcs.mks.realtime;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.intellij.vcs.mks.MKSHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;

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
			while (true) {
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
					addSandboxPath(sandbox.sandboxPath, sandbox.hostAndPort);
				}
			}
		}
	}, "MKS sandbox synchronizer retrier");

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
		return (sandboxInfo==null)?null:sandboxInfo.siSandbox;
	}

	public void addSandboxPath(@NotNull String sandboxPath, final String mksHostAndPort) {

		VirtualFile sandboxVFile = VcsUtil.getVirtualFile(sandboxPath);
		MksSandboxInfo sandboxInfo = new MksSandboxInfo(sandboxPath, mksHostAndPort, sandboxVFile);
		VirtualFile sandboxFolder;
		if (sandboxVFile != null) {
			sandboxFolder = (sandboxVFile.isDirectory()) ? sandboxVFile : sandboxVFile.getParent();
//                if (project.getAllScope().contains(sandboxVFile)) {
			boolean isSandboxInProject = false;
			try {
				isSandboxInProject = project.getProjectScope().contains(sandboxVFile);
			} catch (Throwable e) {
				LOGGER.warn("caught exception while checking if [" + sandboxVFile + "] is in project, postponing check");
				addRejected(sandboxInfo);
			}
			synchronized (lock) {
				if (isSandboxInProject) {
					// ok sandbox in project path
					try {
						TriclopsSiSandbox sandbox = MKSHelper.createSandbox(sandboxPath);
						sandboxInfo.siSandbox = sandbox;
						sandboxByFolder.put(sandboxFolder, sandboxInfo);
						sandboxVFiles.add(sandboxVFile);
						LOGGER.debug("updated sandbox in cache : " + sandboxVFile);
					} catch (TriclopsException e) {
						LOGGER.error("invalid sandbox ? (" + sandboxPath + ")", e);
						addRejected(sandboxInfo);
					}
				} else {
					LOGGER.debug("ignoring out-of-project sandbox " + sandboxVFile);
					outOfScopeSandboxes.add(sandboxInfo);
				}
			}
		} else {
			LOGGER.error("unable to find the virtualFile for " + sandboxPath);
			addRejected(sandboxInfo);
		}

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
	public void dumpStateOn(PrintWriter pw) {
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
		System.err.println("rootsChanged");
		synchronized (lock) {
			List<MksSandboxInfo> allSandboxes = new ArrayList<MksSandboxInfo>(sandboxVFiles.size() + outOfScopeSandboxes.size());
			allSandboxes.addAll(sandboxByFolder.values());
			allSandboxes.addAll(outOfScopeSandboxes);
			clear();
			for (MksSandboxInfo sandbox : allSandboxes) {
				addSandboxPath(sandbox.sandboxPath, sandbox.hostAndPort);
			}
		}
	}

	public Set<MksSandboxInfo> getSandboxesIntersecting(final VirtualFile directory) {
		Set<MksSandboxInfo> result = new HashSet<MksSandboxInfo>();
		for (MksSandboxInfo sandboxInfo : sandboxByFolder.values()) {
			VirtualFile sandboxFile = sandboxInfo.sandboxPjFile;
			if (VfsUtil.isAncestor(directory, sandboxFile, false)) {
				result.add(sandboxInfo);
			} else
			if (sandboxFile.getParent() != null && VfsUtil.isAncestor(sandboxFile.getParent(), directory, false)) {
				result.add(sandboxInfo);
			}
		}
		return result;
	}

	public boolean isPartOfSandbox(final VirtualFile file) {
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

	public MksSandboxInfo getSandboxInfo(@NotNull final VirtualFile virtualFile) {
		MksSandboxInfo sandbox = null;
		VirtualFile cursorDir = (virtualFile.isDirectory() ? virtualFile : virtualFile.getParent());
		for (; cursorDir != null && sandbox == null; cursorDir = cursorDir.getParent())
		{
			sandbox = sandboxByFolder.get(cursorDir);
		}
		return sandbox;
	}
}