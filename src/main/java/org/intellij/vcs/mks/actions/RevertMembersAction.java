package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.RevertTriclopsCommand;
import org.jetbrains.annotations.NotNull;

public class RevertMembersAction extends MultipleTargetAction {

	public RevertMembersAction() {
		super(new RevertTriclopsCommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
		final FileStatusManager statusManager = FileStatusManager.getInstance(project);
		for (VirtualFile vFile : vFiles) {
			final FileStatus status = statusManager.getStatus(vFile);
			if (status == FileStatus.DELETED_FROM_FS || status == FileStatus.OBSOLETE
					|| status == FileStatus.HIJACKED || status == FileStatus.SWITCHED || status == FileStatus.ADDED
					|| status == FileStatus.MERGE || status == FileStatus.MERGED_WITH_CONFLICTS
					|| status == FileStatus.MODIFIED) {
				return true;
			}
		}
		return false;
	}
}
