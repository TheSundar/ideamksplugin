package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class CheckinAction extends MultipleTargetAction {

	public CheckinAction() {
	}

	@Override
	protected void perform(TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.checkinMembers(siMembers, 0);
	}

	@Override
	@NotNull
	protected String getActionName(@NotNull AbstractVcs vcs) {
		return "CheckIn";
	}

	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile file) {
		return true;
	}
}
