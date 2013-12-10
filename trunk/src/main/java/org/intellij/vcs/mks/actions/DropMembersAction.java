package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.api.DropMemberAPICommand;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class DropMembersAction extends MultipleTargetAction {

	public DropMembersAction() {
		super(new DropMemberAPICommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
		// todo may check if file exists in vcs
		return true;
	}
}
