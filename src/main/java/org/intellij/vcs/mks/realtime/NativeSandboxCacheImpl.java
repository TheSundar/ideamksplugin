package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksMemberState;
import org.intellij.vcs.mks.sicommands.AbstractViewSandboxCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Thibaut Fagart
 */
public class NativeSandboxCacheImpl extends AbstractSandboxCacheImpl {

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
					LOGGER.warn(Thread.currentThread().getName() + " interupted, terminating");
					Thread.currentThread().interrupt();
					return;
				}
				final ArrayList<MksSandboxInfo> tempList;
				synchronized (pendingUpdates) {
					tempList = new ArrayList<MksSandboxInfo>(pendingUpdates.size());
					tempList.addAll(pendingUpdates);
					pendingUpdates.clear();
				}
                for (Iterator<MksSandboxInfo> iterator = tempList.iterator(); iterator.hasNext(); ) {
                    MksNativeSandboxInfo next = (MksNativeSandboxInfo) iterator.next();
					LOGGER.debug("re-adding" + next);
					addSandboxBelongingToProject(next);
				}
			}
		}
	}, "MKS sandbox synchronizer retrier");
	private boolean stopBackgroundThread = false;
	private static final int MAX_RETRY = 5;

	public NativeSandboxCacheImpl(final Project project) {
        super(project);
        backgroundUpdates.setDaemon(true);
		backgroundUpdates.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		backgroundUpdates.start();
	}


    @Override
    protected MksSandboxInfo createSandboxInfo(String sandboxPath, String mksHostAndPort, String mksProject, String devPath, boolean isSubSandbox, VirtualFile sandboxVFile) {
        return new MksNativeSandboxInfo(sandboxPath, mksHostAndPort, mksProject, devPath, sandboxVFile, isSubSandbox);
    }

    @Override
    protected void addRejected(final MksSandboxInfo sandbox) {
		sandbox.retries++;
		if (sandbox.retries > MAX_RETRY) {
			LOGGER.debug("giving up sandbox after too many retries " + sandbox.sandboxPath);
		}
		synchronized (pendingUpdates) {
			pendingUpdates.add(sandbox);
		}
	}

    /*
	public boolean isPartOfSandbox(@NotNull final VirtualFile file) {
		return getSandboxInfo(file) != null;
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
*/


    public void release() {
		stopBackgroundThread = true;
        this.pendingUpdates.clear();
        super.release();
	}
}
