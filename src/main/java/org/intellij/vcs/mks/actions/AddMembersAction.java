package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.AddMemberCommand;
import org.jetbrains.annotations.NotNull;

public class AddMembersAction extends MultipleTargetAction {

	public AddMembersAction() {
		super(new AddMemberCommand());
	}


	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile file) {
		// todo may check if the file is not already present in vcs
		return true;
	}
}
