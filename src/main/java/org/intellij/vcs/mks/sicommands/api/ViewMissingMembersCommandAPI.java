package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.WorkItem;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ViewMissingMembersCommandAPI extends ViewSandboxCommandAPI {

    public ViewMissingMembersCommandAPI(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration, @NotNull String sandboxPjPath) {
        super(errors, mksCLIConfiguration, sandboxPjPath);
    }

	@Override
	protected Command createAPICommand() {
		Command command = super.createAPICommand();
		command.addOption(new Option("filter", "changed:missing"));
		return command;
	}

	@Override
    protected MksMemberState createState(WorkItem item) throws VcsException {
        MksMemberState state = super.createState(item);
        return new MksMemberState(state.workingRevision, state.memberRevision, state.workingChangePackageId,  MksMemberState.Status.MISSING);
    }
}
