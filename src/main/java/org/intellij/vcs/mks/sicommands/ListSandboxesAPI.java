package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.IntegrationPoint;
import com.mks.api.IntegrationPointFactory;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksCLIConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Thibaut Fagart
 */
public class ListSandboxesAPI extends SiAPICommand {
	private static final String LINE_SEPARATOR = " -> ";
	public ArrayList<String> sandboxes;
	public static final String COMMAND = "sandboxes";

	public ListSandboxesAPI(List<VcsException> errors, MksCLIConfiguration mksCLIConfiguration) {
		super(errors, COMMAND, mksCLIConfiguration);
	}


	@Override
	public void execute() {
		ArrayList<String> tempSandboxes = new ArrayList<String>();
		try {
			Response response = getAPIHelper().getSICommands().getSandboxes(false);

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
                String sandboxName = sandboxWorkItem.getField("sandboxName").getString();
                tempSandboxes.add(sandboxName);
                debug(sandboxWorkItem, sandboxName);

            }
			sandboxes = tempSandboxes;
		} catch (APIException e) {
            errors.add(new VcsException(e));
        }
    }

    private void debug(WorkItem sandboxWorkItem, String sandboxName) {
        for (Iterator it = sandboxWorkItem.getFields(); it.hasNext();) {
            Field field = (Field) it.next();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("viewsandboxes : sandbox  " +sandboxName );
            }

//                    System.out.println("\t" + field.getName() + " : " + field.getValue());
        }
    }

    @Override
	public String toString() {
		return "ListSandboxes";
	}

}
