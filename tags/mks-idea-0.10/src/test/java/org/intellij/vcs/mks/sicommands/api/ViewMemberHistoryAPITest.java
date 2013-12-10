package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MKSAPIHelper;

import java.util.ArrayList;

public class ViewMemberHistoryAPITest extends  AbstractAPITest {

    public void testCommand() {
        ViewMemberHistoryAPICommand cmd = new ViewMemberHistoryAPICommand(new ArrayList<VcsException>(), getMksCLIConfiguration(), viewMemberHistoryMember) {
            @Override
            protected MKSAPIHelper getAPIHelper() {
                return apiHelper;
            }
        };
        cmd.execute();
        System.out.println(cmd.getRevisionsInfo());

    }
    public void testRunner() throws APIException {
        final CmdRunner runner = apiHelper.getSession().createCmdRunner();
        Command command = new Command(Command.SI);
        command.setCommandName("viewhistory");
        command.addSelection(viewMemberHistoryMember);
        runner.execute(command);
        try {
            Response response = runner.execute(command);
            final SubRoutineIterator routineIterator = response.getSubRoutines();
            while (routineIterator.hasNext()) {
                final SubRoutine subRoutine = routineIterator.next();
                System.err.println("routine " + subRoutine);
            }
            final WorkItemIterator workItems = response.getWorkItems();

            while (workItems.hasNext()) {

                final WorkItem workItem = workItems.next();
                debug("", workItem);
/*
                System.out.println("item");
//                types.add(workItem.getField("type").getValueAsString());
                for (Iterator it = workItem.getFields(); it.hasNext(); ) {
                    Field field = (Field) it.next();
                    System.out.println("\t" + field.getName() + " : " + field.getValue());
                }
*/

            }
            System.out.println("response " + response);
/*
            for (String type : types) {
                System.out.println("type : "+type);
            }
            for (String type : modeltypes) {
                System.out.println("modeltype : "+type);
            }
*/
        } catch (APIException e) {
            System.err.println(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
