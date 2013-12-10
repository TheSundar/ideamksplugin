package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;


public class MemberHistoryAction extends SingleTargetAction {

	public MemberHistoryAction() {
	}

	@Override
	protected void perform(@NotNull TriclopsSiMembers members) throws TriclopsException {
		MKSHelper.openMemberArchiveView(members, 0);
	}

	@Override
	@NotNull
	protected String getActionName(@NotNull AbstractVcs vcs) {
		return "Member History";
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		return
			super.isEnabled(project, vcs, virtualFiles)
				&& !virtualFiles[0].isDirectory() && virtualFiles[0].isValid();
	}
}
