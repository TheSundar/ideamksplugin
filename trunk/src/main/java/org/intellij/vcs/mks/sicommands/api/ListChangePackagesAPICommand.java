package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.model.MksServerInfo;

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
            errors.add(new VcsException(e));
        }

/*
        try {
            Response response = getAPIHelper().getSICommands().getChangePackages(null);
        } catch (APIException e) {
            errors.add(new VcsException(e));
        }
*/

    }

}
