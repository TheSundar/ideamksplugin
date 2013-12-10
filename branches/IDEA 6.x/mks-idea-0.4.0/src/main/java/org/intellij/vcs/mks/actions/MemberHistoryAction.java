package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.module.Module;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.MKSHelper;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class MemberHistoryAction extends BasicAction {

	public MemberHistoryAction() {
	}

	protected void perform(Project project, Module module, MksVcs vcs, VirtualFile file, DataContext dataContext)
			throws VcsException {
		TriclopsSiMembers members = createSiMembers(file, vcs);
		try {
			MKSHelper.openMemberArchiveView(members,0);
		}
		catch (TriclopsException e) {
			if (!MksVcs.isLastCommandCancelled())
				throw new VcsException("History Error: " + MksVcs.getMksErrorMessage());
		}
	}

	protected String getActionName(AbstractVcs vcs) {
		return "Member History";
	}

	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file) {
		return !file.isDirectory() && file.isValid();
	}
}
