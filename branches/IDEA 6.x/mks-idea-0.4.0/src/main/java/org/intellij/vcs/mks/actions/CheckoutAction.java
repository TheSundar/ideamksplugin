package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.module.Module;
import com.intellij.peer.PeerFactory;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;
import mks.integrations.common.TriclopsSiMember;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class CheckoutAction extends BasicAction {

	public CheckoutAction() {
	}

	protected void perform(Project project, VirtualFile[] affectedFiles, DataContext dataContext, List exceptions) {
		List <VirtualFile> filesToCheckout = new ArrayList<VirtualFile>(affectedFiles.length);
		for (VirtualFile file : affectedFiles) {
			if (appliesTo(file)) {
				filesToCheckout.add(file);
			}
		}

		Map<TriclopsSiSandbox, ArrayList<VirtualFile>> filesBySandbox = MksVcs.dispatchBySandbox(filesToCheckout.toArray(new VirtualFile[0]));
		for (Map.Entry<TriclopsSiSandbox, ArrayList<VirtualFile>> entry : filesBySandbox.entrySet()) {
			try {
				TriclopsSiSandbox sandbox = entry.getKey();
				TriclopsSiMembers members = MKSHelper.createMembers(sandbox);
				for (VirtualFile file : entry.getValue()) {
					members.addMember(new TriclopsSiMember(file.getPresentableUrl()));
				}

				MKSHelper.getMembersStatus(members);
				try {
					MKSHelper.checkoutMembers(members,0);
				}
				catch (TriclopsException e) {
					if (MksVcs.isLastCommandCancelled()) {
						exceptions.add(new VcsException("Checkout Error: " + MksVcs.getMksErrorMessage()));
					}
				}

			} catch (TriclopsException e) {
				exceptions.add(new VcsException("Unable to obtain file status"));
			}

		}
		WindowManager.getInstance().getStatusBar(project).setInfo("CheckOut complete.");
	}

	protected void perform(Project project, Module module, MksVcs vcs, VirtualFile file, DataContext dataContext)
			throws VcsException {
		throw new UnsupportedOperationException();
//		if (!appliesTo(file)) {
//			return;
//		}
//
//		TriclopsSiMembers members = createSiMembers(file, vcs);
//		try {
//			MKSHelper.checkoutMembers(members,0);
//		}
//		catch (TriclopsException e) {
//			if (MksVcs.isLastCommandCancelled()) {
//				throw new VcsException("Checkout Error: " + MksVcs.getMksErrorMessage());
//			}
//		}
//		WindowManager.getInstance().getStatusBar(project).setInfo("CheckOut complete.");
 	}


	protected String getActionName(AbstractVcs vcs) {
		return "Check out";
	}

	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file) {
		FilePath filePathOn = PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(file);
		return vcs.fileExistsInVcs(filePathOn);
	}
}
