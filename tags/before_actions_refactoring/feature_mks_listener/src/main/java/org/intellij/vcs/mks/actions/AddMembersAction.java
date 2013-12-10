package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

public class AddMembersAction extends MultipleTargetAction {

	public AddMembersAction() {
	}

	@Override
	protected void perform(TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.addMembers(siMembers, 0);
	}

	@Override
	@NotNull
	protected String getActionName(@NotNull AbstractVcs vcs) {
		return "AddMembers";
	}

	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile file) {
		return true;
	}
}
