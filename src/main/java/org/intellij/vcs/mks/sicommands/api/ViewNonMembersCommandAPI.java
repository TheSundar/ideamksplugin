package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.vcsUtil.VcsUtil;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.*;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewNonMembersCommandAPI extends ViewSandboxCommandAPI {

	private static final String COMMAND = "viewnonmembers";
    protected final Map<String, MksMemberState> memberStates = new HashMap<String, MksMemberState>();

    public ViewNonMembersCommandAPI(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration, @NotNull String sandboxPjPath) {
		super(errors, COMMAND, mksCLIConfiguration, sandboxPjPath);
	}

	@Override
	protected Command createAPICommand() {
		Command command = new Command(Command.SI);
		command.setCommandName("viewnonmembers");
		command.addOption(new Option("cwd", sandboxPath.substring(0, sandboxPath.lastIndexOf('\\'))));
		command.addOption(new Option("recurse"));
		command.addOption(new Option("noincludeFormers"));
		return command;
	}

	@Override
	protected MksMemberState createState(WorkItem item) {
		return new MksMemberState(VcsRevisionNumber.NULL, VcsRevisionNumber.NULL, null, MksMemberState.Status.UNVERSIONED);
	}

	@Override
	protected String getMemberStateName(WorkItem item) {
		return item.getField("Absolute Path").getValueAsString();    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	protected boolean shouldSkip(WorkItem item) {
		return false;
	}

	protected void setState(@NotNull final String name, @NotNull final MksMemberState memberState) {
		memberStates.put(VcsUtil.getFilePath(name).getPath(), memberState);
    }

    public Map<String, MksMemberState> getMemberStates() {
        return Collections.unmodifiableMap(memberStates);
    }
}
