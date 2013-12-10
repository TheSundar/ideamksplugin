package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.module.Module;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.MKSHelper;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class CheckinAction extends BasicAction {

	public CheckinAction() {
	}

	protected void perform(Project project, Module module, MksVcs vcs, VirtualFile file, DataContext dataContext)
			throws VcsException {
		TriclopsSiMembers members = createSiMembers(file, vcs);
		try {
			MKSHelper.checkinMembers(members, 0);
		}
		catch (TriclopsException e) {
			if (!MksVcs.isLastCommandCancelled())
				throw new VcsException("Checkin Error: " + MksVcs.getMksErrorMessage());
		}
		AbstractVcsHelper.getInstance(project).markFileAsUpToDate(file);
		WindowManager.getInstance().getStatusBar(project).setInfo("CheckIn complete.");
	}

	protected String getActionName(AbstractVcs vcs) {
		return "Check In";
	}

	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file) {
		return true;
	}
}
