package org.intellij.vcs.mks.actions.api;

import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AboutMksAPICommand extends AbstractAPICommand {

    public void executeCommand(@NotNull final MksVcs mksVcs, @NotNull List<VcsException> exceptions,
                               @NotNull VirtualFile[] affectedFiles) throws VcsException {
        try {
            final CmdRunner runner = MKSAPIHelper.getInstance().getSession().createCmdRunner();
            Command command = new Command(Command.SI);
            command.setCommandName("about");
            command.addOption(new Option("gui"));
            runner.execute(command);
        } catch (APIException e) {
            ArrayList<VcsException> errors = new ArrayList<VcsException>();
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
            MksVcs.getInstance(mksVcs.getProject()).showErrors(errors, "About MKS");
        }
    }

    @NotNull
    public String getActionName(@NotNull AbstractVcs vcs) {
        return MksBundle.message("action.about.mks");
    }
}
