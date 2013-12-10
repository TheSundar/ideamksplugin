package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.model.MksServerInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ListChangePackagesTest extends AbstractAPITest {

    public void testCommand() {
        ListChangePackagesAPICommand command = new ListChangePackagesAPICommand(new ArrayList<VcsException>(), getMksCLIConfiguration(), new MksServerInfo(mksuser, mkshost, mksport))
        {
            @Override
            protected MKSAPIHelper getAPIHelper() {
                return apiHelper;
            }
        };
        command.execute();
        System.out.println(command.changePackages);
    }

    public void testRunner() throws APIException {
        final CmdRunner runner = apiHelper.getSession().createCmdRunner();
        Command command = new Command(Command.SI);
        command.setCommandName("viewcps");
        MultiValue mv = new MultiValue(",");
        mv.add("id");
        mv.add("user");
        mv.add("state");
        mv.add("summary");
        command.addOption(new Option("fields", mv));
        runner.execute(command);
        final Response response;
        response = runner.execute(command);
        final SubRoutineIterator routineIterator = response.getSubRoutines();
        while (routineIterator.hasNext()) {
            final SubRoutine subRoutine = routineIterator.next();
            System.err.println("routine " + subRoutine);
        }
        final WorkItemIterator workItems = response.getWorkItems();

        while (workItems.hasNext()) {

            final WorkItem workItem = workItems.next();
            debug("", workItem);

        }


    }
}
