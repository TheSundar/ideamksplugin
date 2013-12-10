package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.vcs.AbstractVcs;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.jetbrains.annotations.NotNull;

public class RevertTriclopsCommand extends AbstractMultipleTargetTriclopsCommand {
	@Override
	protected void perform(@NotNull TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.revertMembers(siMembers, 0);
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return "Revert";
	}


}
