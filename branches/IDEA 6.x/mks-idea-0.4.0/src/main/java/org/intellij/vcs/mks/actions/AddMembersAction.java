package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.module.Module;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class AddMembersAction extends BasicAction {

	public AddMembersAction() {
	}

	protected void perform(Project project, Module module, MksVcs vcs, VirtualFile file, DataContext dataContext)
			throws VcsException {
		TriclopsSiMembers members = createSiMembers(file, vcs);
		try {
			MKSHelper.addMembers(members, 0);
		}
		catch (TriclopsException e) {
			if (!MksVcs.isLastCommandCancelled())
				throw new VcsException("Add Error: " + MksVcs.getMksErrorMessage());
		}
		WindowManager.getInstance().getStatusBar(project).setInfo("Member(s) added.");
	}

	protected String getActionName(AbstractVcs vcs) {
		return "Adding files to " + vcs.getName();
	}

	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file) {
		return true;
	}

	protected boolean appliesTo(VirtualFile file) {
		return true;
	}

}