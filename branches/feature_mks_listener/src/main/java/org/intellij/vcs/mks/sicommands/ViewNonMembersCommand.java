package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksMemberState;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewNonMembersCommand extends SiCLICommand {
	private final Map<String, MksMemberState> memberStates = new HashMap<String, MksMemberState>();
	@org.jetbrains.annotations.NonNls
	public static final String COMMAND = "viewnonmembers";

	/**
	 * @param errors
	 * @param encodingProvider
	 * @param sandbox		  must be a directory
	 */
	public ViewNonMembersCommand(@NotNull List<VcsException> errors, @NotNull EncodingProvider encodingProvider, MksSandboxInfo sandbox) {
		super(errors, encodingProvider, COMMAND, "--fields=absolutepath", "--recurse", "--sandbox=" + sandbox.sandboxPath,
				"--hostname=" + sandbox.hostAndPort.substring(0, sandbox.hostAndPort.indexOf(':')));
		setWorkingDir(new File(VcsUtil.getFilePath(sandbox.sandboxPath).getParentPath().getPath()));
	}

	@Override
	public void execute() {
		try {
			executeCommand();
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
			return;
		}
		BufferedReader reader = new BufferedReader(new StringReader(commandOutput));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				final String path = VcsUtil.getFilePath(line).getPath();
				MksMemberState state = new MksMemberState(VcsRevisionNumber.NULL, VcsRevisionNumber.NULL, null, MksMemberState.Status.UNVERSIONED);
				memberStates.put(path, state);
			}
		} catch (IOException e) {
			LOGGER.error("error reading output " + commandOutput, e);
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}
	}

	public Map<String, MksMemberState> getMemberStates() {
		return memberStates;
	}
}
