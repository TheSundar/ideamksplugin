package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.vcs.AbstractVcs;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksBundle;
import org.jetbrains.annotations.NotNull;

public class MemberHistoryTriclopsCommand extends AbstractSingleTargetTriclopsCommand {
	protected void perform(@NotNull TriclopsSiMembers members) throws TriclopsException {
		MKSHelper.openMemberArchiveView(members, 0);
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.member.history");
	}


}
