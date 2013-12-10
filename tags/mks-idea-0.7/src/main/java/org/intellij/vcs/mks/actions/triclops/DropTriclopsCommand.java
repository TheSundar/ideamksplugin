package org.intellij.vcs.mks.actions.triclops;

import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksBundle;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.vcs.AbstractVcs;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;

public class DropTriclopsCommand extends AbstractMultipleTargetTriclopsCommand {
	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.drop");
	}

	@Override
	protected void perform(@NotNull TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.dropMembers(siMembers, 0);
	}
}
