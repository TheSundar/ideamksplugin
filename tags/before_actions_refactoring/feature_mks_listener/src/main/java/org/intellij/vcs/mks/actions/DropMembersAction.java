package org.intellij.vcs.mks.actions;

import com.intellij.openapi.vcs.AbstractVcs;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class DropMembersAction extends MultipleTargetAction {

	public DropMembersAction() {
	}

	@Override
	protected void perform(TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.dropMembers(siMembers, 0);
	}

	@Override
	@NotNull
	protected String getActionName(@NotNull AbstractVcs vcs) {
		return "Drop Members";
	}
}
