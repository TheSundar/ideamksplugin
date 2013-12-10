package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.vcs.AbstractVcs;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksBundle;
import org.jetbrains.annotations.NotNull;

public class MemberInformationTriclopsCommand extends AbstractSingleTargetTriclopsCommand {
	protected void perform(@NotNull TriclopsSiMembers members) throws TriclopsException {
		MKSHelper.openMemberInformationView(members, 0);
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.member.information");
	}

}
