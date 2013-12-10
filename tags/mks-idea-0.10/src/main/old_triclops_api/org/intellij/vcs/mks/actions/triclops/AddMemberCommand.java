package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.vcs.AbstractVcs;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.actions.MksCommand;
import org.jetbrains.annotations.NotNull;

public class AddMemberCommand extends AbstractMultipleTargetTriclopsCommand implements MksCommand {
	protected void perform(@NotNull TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.addMembers(siMembers, 0);
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.addmembers");
	}

}
