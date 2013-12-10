package org.intellij.vcs.mks.actions;

import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.ResyncTriclopsCommand;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class ResynchronizeMembersAction extends MultipleTargetAction {

	public ResynchronizeMembersAction() {
		super(new ResyncTriclopsCommand());
	}


	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile file) {
		return true;
	}
}
