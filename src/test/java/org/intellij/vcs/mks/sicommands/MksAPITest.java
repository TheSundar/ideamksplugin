/*
 * COPYRIGHT. HSBC HOLDINGS PLC 2008. ALL RIGHTS RESERVED.
 *
 * This software is only to be used for the purpose for which it has been
 * provided. No part of it is to be reproduced, disassembled, transmitted,
 * stored in a retrieval system nor translated in any human or computer
 * language in any way or for any other purposes whatsoever without the
 * prior written consent of HSBC Holdings plc.
 */
package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.*;
import com.mks.api.response.*;
import com.mks.connect.CmdRunnerImpl;
import junit.framework.TestCase;
import org.intellij.vcs.mks.CommandExecutionListener;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.intellij.vcs.mks.sicommands.api.ViewMemberHistoryAPICommand;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MksAPITest extends TestCase {
    private static final String user = "79310750";
    private static final String password = "r3gu7ar";
    private static final String server = "vhvhcl50.us.hsbc";
    private static final int port = 7001;
    private static final String sandbox = "c:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services\\project.pj";
    private Session session;
    private static final String MEMBER_FOR_HISTORY = "C:\\Documents and Settings\\A6253567\\sandboxes\\P2G_HBFR_7.0\\HBFR_IDV\\HbfrIdv\\src\\main\\resources\\com\\hsbc\\hbfr\\idv\\resources\\HBFRRegistrationForm\\interceptor-chain.xml";

    protected void setUp() throws Exception {
		super.setUp();
		setupLocalIntegrationPoint();
	}

	private void setupRemoteIntegrationPoint() throws APIException {
		final IntegrationPoint point =
				IntegrationPointFactory.getInstance().
						createIntegrationPoint(server, port);

		session = point.createSession(user, password);
	}

	private void setupLocalIntegrationPoint() throws APIException {
		final IntegrationPoint point =
				IntegrationPointFactory.getInstance().
						createLocalIntegrationPoint();

		session = point.getCommonSession(user, password);
//		session = point.createSession();
//		session = point.createSession("e9310750","e9310750");
	}

	protected void tearDown() throws Exception {
		session.release();
		super.tearDown();	//To change body of overridden methods use File | Settings | File Templates.
	}

	/**
	 * item structure
	 * item
	 * locker : null
	 * workingLockInfo : null
	 * memberLockInfo : null
	 * workingrev : 1.2.1.3
	 * workingcpid : null
	 * deferred : false
	 * type : archived
	 * name : c:/temp/test-mks/target/checkout/.classpath
	 * memberrev : 1.2.1.3
	 * locksandbox : null
	 *
	 * @throws APIException
	 */
	public void testViewSandbox() throws APIException {
		final CmdRunner runner = session.createCmdRunner();
		Command command = new Command(Command.SI);
		command.setCommandName("viewsandbox");
		command.addOption(new Option("sandbox", sandbox));
//		command.addOption(new Option("user","e9310750"));
//		command.addOption(new Option("password","e9310750"));
		command.addOption(
				new Option("fields", "locker,workingrev,workingcpid,deferred,type,name,memberrev,locksandbox"));
		System.err.println(command.toString());
		final Response response;
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
				for (Iterator it = workItem.getFields(); it.hasNext();) {
					Field field = (Field) it.next();
					System.out.println("\t" + field.getName() + " : " + field.getValue());
				}

			}
			System.out.println("response " + response);
		} catch (APIException e) {
			System.err.println(e);
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}

	public void testViewSandboxes() throws APIException {
		final CmdRunner runner = session.createCmdRunner();
		Command command = new Command(Command.SI);
		command.setCommandName("sandboxes");
//		command.addOption(new Option("user","e9310750"));
//		command.addOption(new Option("password","e9310750"));
		System.err.println(command.toString());
		final Response response;
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
				for (Iterator it = workItem.getFields(); it.hasNext();) {
					Field field = (Field) it.next();
					System.out.println("\t" + field.getName() + " : " + field.getValue());
				}

			}
			System.out.println("response " + response);
		} catch (APIException e) {
			System.err.println(e);
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}

	public void testViewSandboxWithXML() throws APIException {
		final CmdRunner runner = session.createCmdRunner();
		final String response;
		try {
			response = ((CmdRunnerImpl) runner).executeWithXML(new String[]{"si", "viewsandbox",
					"--sandbox=" + sandbox,
					"--fields=locker,workingrev,workingcpid,deferred,type,name,memberrev,locksandbox",
					"--user=" + user,
					"--password=" +password});
			System.out.println("response " + response);
		} catch (APIException e) {
			System.err.println(e);
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}

	public void testViewSandboxesWithXML() throws APIException {
		final CmdRunner runner = session.createCmdRunner();
		final String response;
		try {
			response = ((CmdRunnerImpl) runner).executeWithXML(new String[]{"si", "sandboxes",
					"--user=" + user,
					"--password=" +password});
			System.out.println("response " + response);
		} catch (APIException e) {
			System.err.println(e);
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}

	public void testMemberHistory() throws APIException {
		final CmdRunner runner = session.createCmdRunner();
		Command command = new Command(Command.SI);
		command.setCommandName("viewhistory");
//		command.addOption(new Option("sandbox","c:\\temp\\test-mks\\target\\checkout\\project.pj"));
//		command.addOption(new Option("user","e9310750"));
//		command.addOption(new Option("password","e9310750"));
		command.addOption(new Option("fields", "revision,date,author,cpid,description"));
		command.addSelection( MEMBER_FOR_HISTORY);
		System.err.println(command.toString());
		final Response response;
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
				System.out.println("WorkItem " + workItem.getId());
//                workItem.getField("revisions");
				final List memberRevisions = (List) workItem.getField("revisions").getValue();

				for (Iterator it = workItem.getFields(); it.hasNext();) {
					Field field = (Field) it.next();
					final Object value = field.getValue();
					if (value instanceof ItemList) {
						ItemList valueItemList = (ItemList) value;
						int idx = 0;
						for (Iterator it2 = valueItemList.iterator(); it2.hasNext(); idx++) {
							Item item = (Item) it2.next();
							for (Iterator it3 = item.getFields(); it3.hasNext();) {
								Field revisionField = (Field) it3.next();
								final Object revisionFieldValue = revisionField.getValue();
								final String revisionFieldName = revisionField.getName();

								System.out.println(
										"\t" + field.getName() + "." + idx + "." + revisionFieldName + " : " +
												revisionFieldValue);
							}
						}
					}
					System.out.println("\t" + field.getName() + " : " + value);
				}

			}
			System.out.println("response " + response);
		} catch (APIException e) {
			System.err.println(e);
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}
    public void testListServers() throws APIException {
        final CmdRunner runner = session.createCmdRunner();
        Command command = new Command(Command.SI);
        command.setCommandName("servers");
//		command.addOption(new Option("user","e9310750"));
//		command.addOption(new Option("password","e9310750"));
        System.err.println(command.toString());
        final Response response;
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
                for (Iterator it = workItem.getFields(); it.hasNext();) {
                    Field field = (Field) it.next();
                    System.out.println("\t" + field.getName() + " : " + field.getValue());
                }

            }
            System.out.println("response " + response);
        } catch (APIException e) {
            System.err.println(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
    public void testViewCps() throws APIException {
        final CmdRunner runner = session.createCmdRunner();
        Command command = new Command(Command.SI);
        command.setCommandName("connect");
		command.addOption(new Option("hostname", "source.systems.uk.hsbc"));
		command.addOption(new Option("port","8001"));
		command.addOption(new Option("user","43317205"));
        Response response = null;
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
                for (Iterator it = workItem.getFields(); it.hasNext();) {
                    Field field = (Field) it.next();
                    System.out.println("\t" + field.getName() + " : " + field.getValue());
                }

            }
            System.out.println("response " + response);
        } catch (APIException e) {
            System.err.println(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        command = new Command(Command.SI);
        command.setCommandName("viewcps");
		command.addOption(new Option("hostname", "source.systems.uk.hsbc"));
		command.addOption(new Option("port", "8001"));
		command.addOption(new Option("user","43317205"));
//		command.addOption(new Option("nobatch"));
        System.err.println(command.toString());
//        final Response response;
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
                for (Iterator it = workItem.getFields(); it.hasNext();) {
                    Field field = (Field) it.next();
                    System.out.println("\t" + field.getName() + " : " + field.getValue());
                }

            }
            System.out.println("response " + response);
        } catch (APIException e) {
            System.err.println(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void testLock() {

        String [] members = new String[]{"C:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services\\src\\main\\java\\com\\hsbc\\hbfr\\gct\\messaging\\converter\\GctRequestPopulatorConverter.java"};
        if (members.length <= 0) throw new IllegalArgumentException("empty member list to lock");
        Command command = new Command(Command.SI);
        command.setCommandName("lock");
        command.addOption(new Option("sandbox", "C:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services\\src\\main\\java\\com\\hsbc\\hbfr\\gct\\messaging\\converter\\project.pj"));
//        command.addOption(new Option("nobranch"));
//        command.addOption(new Option("nobranchvariant"));
/*
        if (changePackage != null) {
            command.addOption(new Option("cpid", changePackage.getId()));
        }
*/

        for (int i = 0; i < members.length; i++) {
            String member = members[i];
            command.addSelection(member);
        }
        Response response = null;
        try {
            final CmdRunner runner = session.createCmdRunner();

            response = runner.execute(command);
        } catch (APIException e) {
            e.printStackTrace();
        }


    }

	public void testIDEACommand() {
        ViewMemberHistoryAPICommand command =
				new ViewMemberHistoryAPICommand(new ArrayList<VcsException>(), getMksCLIConfiguration(),
                        MEMBER_FOR_HISTORY)
				;
		command.execute();
		for (MksMemberRevisionInfo revisionInfo : command.getRevisionsInfo()) {
			System.out.println(revisionInfo);
		}
		command =
				new ViewMemberHistoryAPICommand(new ArrayList<VcsException>(), getMksCLIConfiguration(),
                        MEMBER_FOR_HISTORY)
				;
		command.execute();
		for (MksMemberRevisionInfo revisionInfo : command.getRevisionsInfo()) {
			System.out.println(revisionInfo);
		}


	}
    public void testHttpLocation()           {
        URL resource = getClass().getResource("/" + "org.apache.commons.httpclient.MultiThreadedHttpConnectionManager".replace('.', '/') + ".class");
        System.out.println(resource.toExternalForm());

    }

    private MksCLIConfiguration getMksCLIConfiguration() {
        return new MksCLIConfiguration() {
            @NotNull
            public String getMksSiEncoding(final String command) {
                return "";
            }

            @NotNull
            public String getDatePattern() {
                return "MMM dd, yyyy - hh:mm a";
            }

            public CommandExecutionListener getCommandExecutionListener() {
                return CommandExecutionListener.IDLE;
            }

            @Override
            public boolean isMks2007() {
                return false;
            }
        };
    }

}
