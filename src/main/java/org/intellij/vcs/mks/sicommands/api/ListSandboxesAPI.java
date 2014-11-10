package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.Command;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MksCLIConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Thibaut Fagart
 */
public class ListSandboxesAPI extends SiAPICommand {
	public ArrayList<String> sandboxes;
	public static final String COMMAND = "sandboxes";

	public ListSandboxesAPI(List<VcsException> errors, MksCLIConfiguration mksCLIConfiguration) {
		super(errors, COMMAND, mksCLIConfiguration);
	}


	@Override
	protected Command createAPICommand() {
		return new Command();
	}

	@Override
	protected Response executeCommand(Command command) throws APIException {
		return getAPIHelper().getSICommands().getSandboxes(false);
	}

	protected void handleResponse(Response response) throws APIException {
		ArrayList<String> tempSandboxes = new ArrayList<String>();
		final SubRoutineIterator routineIterator = response.getSubRoutines();
		while (routineIterator.hasNext()) {
			final SubRoutine subRoutine = routineIterator.next();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("routine " + subRoutine);
			}
		}
		final WorkItemIterator workItems = response.getWorkItems();
		while (workItems.hasNext()) {
			final WorkItem sandboxWorkItem = workItems.next();
			String sandboxName = sandboxWorkItem.getField("sandboxName").getValueAsString();
			tempSandboxes.add(sandboxName);
			debug(sandboxWorkItem, sandboxName);

		}
		sandboxes = tempSandboxes;
	}

	private void debug(WorkItem sandboxWorkItem, String sandboxName) {
		for (Iterator it = sandboxWorkItem.getFields(); it.hasNext(); ) {
			Field field = (Field) it.next();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("viewsandboxes : sandbox  " + sandboxName);
			}

//                    System.out.println("\t" + field.getName() + " : " + field.getValue());
		}
	}

	@Override
	public String toString() {
		return "ListSandboxes";
	}

}
