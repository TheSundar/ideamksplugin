package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.CheckinTriclopsCommand;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class CheckinAction extends MultipleTargetAction {

	public CheckinAction() {
		super(new CheckinTriclopsCommand());
	}


	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile file) {
		// todo may check if file is present in vcs and has been checkedout
		return true;
	}
}
