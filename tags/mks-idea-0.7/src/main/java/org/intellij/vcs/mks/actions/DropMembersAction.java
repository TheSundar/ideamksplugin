package org.intellij.vcs.mks.actions;

import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.DropTriclopsCommand;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class DropMembersAction extends MultipleTargetAction {

	public DropMembersAction() {
		super(new DropTriclopsCommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
		// todo may check if file exists in vcs
		return true;
	}
}
