package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import mks.integrations.common.TriclopsException;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;

import java.util.ArrayList;

public class AboutMksAction extends AnAction {

    public AboutMksAction() {
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
                    MksVcs.getInstance(project).showErrors(errors, "About MKS");
                }
            }
        });
    }

}
