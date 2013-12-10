package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.module.Module;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.MKSHelper;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class ViewSandboxAction extends BasicAction {

	protected boolean isRecursive() {
		return false;
	}

	public ViewSandboxAction() {
	}

	protected void perform(Project project, Module module, MksVcs vcs, VirtualFile file, DataContext dataContext)
			throws VcsException {
		TriclopsSiSandbox sandbox = null;
		sandbox = vcs.getSandbox(file);
		try {
			MKSHelper.viewSandbox(sandbox);

		} catch (TriclopsException e) {
			throw new VcsException("ViewSandbox:  Unable to view sandbox.");
		}
	}

	protected String getActionName(AbstractVcs vcs) {
		return "View Sandbox";
	}

	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file) {
		return true;
	}
}
