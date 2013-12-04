package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.vcsUtil.VcsUtil;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksMemberState;
import org.intellij.vcs.mks.sicommands.SandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewNonMembersCommandAPI extends SiAPICommand {

    private static final String COMMAND = "viewnonmembers";
    private final String sandboxPath;
    protected final Map<String, MksMemberState> memberStates = new HashMap<String, MksMemberState>();

    public ViewNonMembersCommandAPI(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration, @NotNull String sandboxPjPath) {
        super(errors, COMMAND, mksCLIConfiguration);
        this.sandboxPath = sandboxPjPath;
    }

    @Override
    public void execute() {
        try {
            Command command = new Command(Command.SI);
            command.setCommandName("viewnonmembers");
            command.addOption(new Option("cwd", sandboxPath.substring(0, sandboxPath.lastIndexOf('\\'))));
            command.addOption(new Option("recurse"));
            command.addOption(new Option( "noincludeFormers" ));

            final Response response = executeCommand(command);

            final SubRoutineIterator routineIterator = response.getSubRoutines();
            while (routineIterator.hasNext()) {
                final SubRoutine subRoutine = routineIterator.next();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("routine " + subRoutine);
                }
            }
            final WorkItemIterator workItems = response.getWorkItems();
            while (workItems.hasNext()) {
                final WorkItem item = workItems.next();
                MksMemberState state = new MksMemberState(VcsRevisionNumber.NULL, VcsRevisionNumber.NULL, null,
                        MksMemberState.Status.UNVERSIONED);
                setState(item.getField("Absolute Path").getValueAsString(), state);
            }
        } catch (APIException e) {
            errors.add(new VcsException(e));
        }
    }
    protected void setState(@NotNull final String name, @NotNull final MksMemberState memberState) {
        memberStates.put(VcsUtil.getFilePath(name).getPath(), memberState);
    }

    public Map<String, MksMemberState> getMemberStates() {
        return Collections.unmodifiableMap(memberStates);
    }
}
