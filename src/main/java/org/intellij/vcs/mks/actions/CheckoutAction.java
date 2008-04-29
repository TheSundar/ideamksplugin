package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.CheckoutTriclopsCommand;
import org.jetbrains.annotations.NotNull;


public class CheckoutAction extends MultipleTargetAction {

	public CheckoutAction() {
		super(new CheckoutTriclopsCommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
		final FileStatusManager statusManager = FileStatusManager.getInstance(project);
		for (VirtualFile vFile : vFiles) {
			final FileStatus status = statusManager.getStatus(vFile);
			if (status == FileStatus.NOT_CHANGED || status == FileStatus.DELETED_FROM_FS
					|| status == FileStatus.OBSOLETE || status == FileStatus.HIJACKED ||
					status == FileStatus.SWITCHED) {
				return true;
			}
		}
		return false;
	}
}
