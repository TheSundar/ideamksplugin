package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.VcsException;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListChangePackagesAPICommand extends SiAPICommand {

    public final MksServerInfo serverInfo;
    public List<MksChangePackage> changePackages;

    public ListChangePackagesAPICommand(List<VcsException> errors, MksCLIConfiguration mksCLIConfiguration,
                              final MksServerInfo server) {
        super(errors, "viewcps", mksCLIConfiguration);
        serverInfo = server;
    }

    @Override
    public void execute() {
        Command command = new Command(Command.SI);
        command.setCommandName("viewcps");
        MultiValue mv = new MultiValue(",");
        mv.add("id");
        mv.add("user");
        mv.add("state");
        mv.add("summary");
        command.addOption(new Option("fields", mv));
        command.addOption(new Option("hostname", serverInfo.host));
        command.addOption(new Option("port", serverInfo.port));

        try {
            Response response = executeCommand(command);


            final SubRoutineIterator routineIterator = response.getSubRoutines();
            while (routineIterator.hasNext()) {
                final SubRoutine subRoutine = routineIterator.next();
                System.err.println("routine " + subRoutine);
            }
            final WorkItemIterator workItems = response.getWorkItems();
            List<MksChangePackage> tempChangePackages = new ArrayList<MksChangePackage>();

            while (workItems.hasNext()) {

                final WorkItem workItem = workItems.next();
                String cpid = workItem.getField("id").getValueAsString();
                String user = workItem.getField("user").getValueAsString();
                String state = workItem.getField("state").getValueAsString();
                String summary = workItem.getField("summary").getValueAsString();

                tempChangePackages.add(new MksChangePackage(serverInfo.host, cpid, user, state,
                        summary));


            }
            changePackages = tempChangePackages;
        } catch (APIException e) {
            if (e.getMessage().contains("(it may be down)")) {
                try {
                    final Runnable run = new Runnable() {
                        public void run() {
                            final IsServerSiServerDialog dialog = new IsServerSiServerDialog(serverInfo.host + ":" + serverInfo.port);
                            dialog.show();
                            serverInfo.isSIServer = dialog.isSiServer;
                        }
                    };
                    MksVcs.invokeOnEventDispatchThreadAndWait(run);

                } catch (VcsException e2) {
                    LOGGER.warn(e2.getCause());
                    final Throwable o = e2.getCause();
                    //noinspection ThrowableInstanceNeverThrown
                    errors.add(o instanceof VcsException ? (VcsException) o : e2);
                }

            } else {
                errors.add(new VcsException(e));
            }
        }

/*
        try {
            Response response = getAPIHelper().getSICommands().getChangePackages(null);
        } catch (APIException e) {
            errors.add(new VcsException(e));
        }
*/

    }
    static class IsServerSiServerDialog extends DialogWrapper {
        boolean isSiServer = false;
        private final String serverName;

        IsServerSiServerDialog(@NotNull String serverName) {
            super(false);
            this.serverName = serverName;
            init();
        }

        @Override
        @Nullable
        protected JComponent createCenterPanel() {
            return new JLabel("Is " + serverName + " a source integrity server ?\n" +
                    "It does not seem to accept si commands.\n" +
                    "(Answer yes if it is only momentarily down");
        }

        @Override
        protected Action[] createActions() {
            Action[] actions = new Action[2];
            actions[0] = new AbstractAction("Yes") {
                public void actionPerformed(ActionEvent e) {
                    isSiServer = true;
                    close(1);
                }
            };
            actions[1] = new AbstractAction("No") {
                public void actionPerformed(ActionEvent e) {
                    isSiServer = false;
                    close(1);
                }
            };
            return actions;
        }
    }

}
