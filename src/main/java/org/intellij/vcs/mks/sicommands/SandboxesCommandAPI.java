package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Returns a list of all sandboxes registered on the system, including subsandboxes
 */
public class SandboxesCommandAPI extends SiAPICommand {
    public final ArrayList<SandboxInfo> result = new ArrayList<SandboxInfo>();

    public SandboxesCommandAPI(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration) {
        super(errors, "sandboxes", mksCLIConfiguration);
    }

    public void execute() {
        ArrayList<SandboxInfo> tempSandboxes = new ArrayList<SandboxInfo>();
        try {
            Response response = getAPIHelper().getSICommands().getSandboxes(true);

            final SubRoutineIterator routineIterator = response.getSubRoutines();
            while (routineIterator.hasNext()) {
                final SubRoutine subRoutine = routineIterator.next();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("routine " + subRoutine);
                }
            }
            Map<String, SandboxInfo> sandboxesByPath = new HashMap<String, SandboxInfo>();
            final WorkItemIterator workItems = response.getWorkItems();
            while (workItems.hasNext()) {
                final WorkItem sandboxWorkItem = workItems.next();
//                if (LOGGER.isDebugEnabled()) {
                debug(sandboxWorkItem);
//                }
                String sandboxName = sandboxWorkItem.getField("sandboxName").getString();
                String server = sandboxWorkItem.getField("server").getValueAsString();

                String projectName = sandboxWorkItem.getField("projectName").getString();
                String buildRevision = sandboxWorkItem.getField("buildRevision").getString();
                String devPath = sandboxWorkItem.getField("developmentPath").getString();
                boolean isSubsandbox = sandboxWorkItem.getField("isSubsandbox").getBoolean();
                SandboxInfo sandbox;
                if (!isSubsandbox) {
                    sandbox = new SandboxInfo(sandboxName,server,projectName,buildRevision, devPath);
                } else {
                    String parentSandbox = sandboxWorkItem.getField("parentSandbox").getString();
                    sandbox = new SandboxInfo(sandboxesByPath.get(parentSandbox), sandboxName, projectName, buildRevision, devPath);
                }
                sandboxesByPath.put(sandboxName, sandbox);
                tempSandboxes.add(sandbox);
            }
            sandboxesByPath.clear();
            result.addAll(tempSandboxes);
        } catch (APIException e) {
            errors.add(new VcsException(e));
        }
    }

    private void debug(WorkItem workItem) {
        for (Iterator it = workItem.getFields(); it.hasNext(); ) {
            Field field = (Field) it.next();
            LOGGER.debug("\t" + field.getName() + " : " + field.getValue());
//            System.out.println("\t" + field.getName() + " : " + field.getValue());
        }
    }
}
