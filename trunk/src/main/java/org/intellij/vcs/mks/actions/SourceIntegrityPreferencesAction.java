package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.response.APIException;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksVcs;

import java.util.ArrayList;

public class SourceIntegrityPreferencesAction extends AnAction {

    public SourceIntegrityPreferencesAction() {
    }

    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        try {
            final CmdRunner runner =  MKSAPIHelper.getInstance().getSession().createCmdRunner();
            Command command = new Command(Command.SI);
            command.setCommandName("viewprefs");
            runner.execute(command);
        } catch (APIException e) {
            final Project project = anActionEvent.getData(DataKeys.PROJECT);
            ArrayList<VcsException> errors = new ArrayList<VcsException>();
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
            MksVcs.getInstance(project).showErrors(errors, MksBundle.message("action.mks.preferences"));
        }


    }
}
