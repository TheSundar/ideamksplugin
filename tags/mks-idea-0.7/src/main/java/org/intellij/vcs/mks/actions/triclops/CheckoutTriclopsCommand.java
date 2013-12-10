package org.intellij.vcs.mks.actions.triclops;

import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksBundle;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.vcs.AbstractVcs;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;

public class CheckoutTriclopsCommand extends AbstractMultipleTargetTriclopsCommand {
	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.drop.members");
	}

	protected void perform(@NotNull TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.checkoutMembers(siMembers, 0);
	}
}
