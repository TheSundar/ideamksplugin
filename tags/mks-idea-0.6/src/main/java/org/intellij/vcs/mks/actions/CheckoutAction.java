package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;


public class CheckoutAction extends MultipleTargetAction {

	public CheckoutAction() {
	}

	@Override
	protected void perform(@NotNull TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.checkoutMembers(siMembers, 0);
	}


	@Override
	@NotNull
	protected String getActionName(@NotNull AbstractVcs vcs) {
		return "Check out";
	}

	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile file) {
		FilePath filePathOn = PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(file);
		return vcs.fileExistsInVcs(filePathOn);
	}
}
