package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ViewSandboxAPITest  extends AbstractAPITest{
    static final Set<String> SIMPLE_TYPES = new HashSet<String>(Arrays.asList(Field.BOOLEAN_TYPE, Field.DATE_TYPE, Field.DOUBLE_TYPE,
            Field.FLOAT_TYPE, Field.INTEGER_TYPE, Field.LONG_TYPE, Field.STRING_TYPE));


    public void testCommand() {
        ViewSandboxCommandAPI commandAPI = new ViewSandboxCommandAPI(new ArrayList<VcsException>(), getMksCLIConfiguration(), sandbox) {
            @Override
            protected MKSAPIHelper getAPIHelper() {
                return apiHelper;
            }

            @Override
            protected void setState(@NotNull String name, @NotNull MksMemberState memberState) {
                System.out.println(name +" => "+memberState);
            }
        };
        commandAPI.execute();

    }
    public void testNonMembersCommand() {
        ViewNonMembersCommandAPI commandAPI = new ViewNonMembersCommandAPI(new ArrayList<VcsException>(), getMksCLIConfiguration(), sandbox) {
            @Override
            protected MKSAPIHelper getAPIHelper() {
                return apiHelper;
            }

            @Override
            protected void setState(@NotNull String name, @NotNull MksMemberState memberState) {
                System.out.println(name +" => "+memberState);
            }
        };
        commandAPI.execute();

    }
    public void testViewSandboxInfo() throws APIException {
        final CmdRunner runner = apiHelper.getSession().createCmdRunner();
//        Response response = apiHelper.getSICommands().getSandboxInfo(sandbox.substring(0, sandbox.lastIndexOf('\\')));
/*
        final CmdRunner runner = session.createCmdRunner();
*/
        Command command = new Command(Command.SI);
        command.setCommandName("viewsandbox");
        command.addOption(new Option("sandbox", sandbox));
        MultiValue mv = new MultiValue( "," );
        mv.add( "name" );
        mv.add( "context" );
        mv.add( "wfdelta" );
        mv.add( "memberrev" );
        mv.add( "workingrev" );
        mv.add( "revsyncdelta" );
        mv.add( "memberarchive" );
        mv.add( "cpid" );
        mv.add("workingcpid");
        command.addOption(new Option("fields", mv));
        command.addOption(new Option("recurse"));
        System.err.println(command.toString());
        final Response response;
        final Set<String> types = new HashSet<String>();
        final Set<String> modeltypes = new HashSet<String>();
        try {
            response = runner.execute(command);
            final SubRoutineIterator routineIterator = response.getSubRoutines();
            while (routineIterator.hasNext()) {
                final SubRoutine subRoutine = routineIterator.next();
                System.err.println("routine " + subRoutine);
            }
            final WorkItemIterator workItems = response.getWorkItems();

            while (workItems.hasNext()) {

                final WorkItem workItem = workItems.next();
                debug(workItem);

                try {
                    types.add(workItem.getField("type").getValueAsString());
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }
                modeltypes.add(workItem.getModelType());
/*
                for (Iterator it = workItem.getFields(); it.hasNext(); ) {
                    Field field = (Field) it.next();
                    System.out.println("\t" + field.getName() + " : " + field.getValue());
                }
*/

            }
            System.out.println("response " + response);
            for (String type : types) {
                System.out.println("type : "+type);
            }
            for (String type : modeltypes) {
                System.out.println("modeltype : "+type);
            }
        } catch (APIException e) {
            System.err.println(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void debug(Item item) {
        String tab = "";
        debug(tab, item);
    }


    public void testViewNonMembers() throws APIException {
        final CmdRunner runner = apiHelper.getSession().createCmdRunner();
//        Response response = apiHelper.getSICommands().getSandboxInfo(sandbox.substring(0, sandbox.lastIndexOf('\\')));
/*
        final CmdRunner runner = session.createCmdRunner();
*/
        Command command = new Command(Command.SI);
        command.setCommandName("viewnonmembers");
        command.addOption(new Option("cwd", sandbox.substring(0, sandbox.lastIndexOf('\\'))));
        command.addOption(new Option("recurse"));
        command.addOption(new Option( "noincludeFormers" ));
/*
        command.addOption(
                new Option("fields", "locker,workingrev,workingcpid,deferred,type,name,memberrev,locksandbox"));
*/
        System.err.println(command.toString());
        final Response response;
        final Set<String> types = new HashSet<String>();
        final Set<String> modeltypes = new HashSet<String>();
        try {
            response = runner.execute(command);
            final SubRoutineIterator routineIterator = response.getSubRoutines();
            while (routineIterator.hasNext()) {
                final SubRoutine subRoutine = routineIterator.next();
                System.err.println("routine " + subRoutine);
            }
            final WorkItemIterator workItems = response.getWorkItems();

            while (workItems.hasNext()) {

                final WorkItem workItem = workItems.next();
                System.out.println("item");
//                types.add(workItem.getField("type").getValueAsString());
                modeltypes.add(workItem.getModelType());
                for (Iterator it = workItem.getFields(); it.hasNext(); ) {
                    Field field = (Field) it.next();
                    System.out.println("\t" + field.getName() + " : " + field.getValue());
                }

            }
            System.out.println("response " + response);
            for (String type : types) {
                System.out.println("type : "+type);
            }
            for (String type : modeltypes) {
                System.out.println("modeltype : "+type);
            }
        } catch (APIException e) {
            System.err.println(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
/*
    public void testIt() throws APIException {
        apiHelper.getSICommands().siSandboxView(sandbox.substring(0, sandbox.lastIndexOf('\\')));

    }
*/

}
