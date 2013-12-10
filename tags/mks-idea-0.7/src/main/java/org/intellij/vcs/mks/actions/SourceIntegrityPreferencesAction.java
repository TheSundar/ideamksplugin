package org.intellij.vcs.mks.actions;

import java.util.ArrayList;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksVcs;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import mks.integrations.common.TriclopsException;

public class SourceIntegrityPreferencesAction extends AnAction {

	public SourceIntegrityPreferencesAction() {
	}

	@Override
	public void actionPerformed(final AnActionEvent anActionEvent) {

		ApplicationManager.getApplication().runReadAction(new Runnable() {
			public void run() {
				try {
					MKSHelper.aboutBox();
				} catch (TriclopsException e) {
					final Project project = anActionEvent.getData(DataKeys.PROJECT);
					ArrayList<VcsException> errors = new ArrayList<VcsException>();
					//noinspection ThrowableInstanceNeverThrown
					errors.add(new VcsException(e));
					MksVcs.getInstance(project).showErrors(errors, MksBundle.message("action.mks.preferences"));
				}
			}
		});
	}
}
