package org.intellij.vcs.mks.sicommands.cli;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This command allows to unlock a varying number of members.
 * All the members must belong to the same sandbox or the command will fail.
 * IF the members have been locked by someoneelse, they won't be unlocked
 */
public class UnlockMemberCommand extends SiCLICommand {
	@NotNull
	private final String[] members;
	@org.jetbrains.annotations.NonNls
	public static final String COMMAND = "unlock";

	public UnlockMemberCommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration,
							   @NotNull MksSandboxInfo sandbox, String... members) {
		super(errors, mksCLIConfiguration, COMMAND,
				createArray(members, "--sandbox=" + sandbox.sandboxPath, "--nobreaklock"));
		assert members.length > 0 : "need to specify which member to lock";
		setWorkingDir(new File(VcsUtil.getFilePath(sandbox.sandboxPath).getParentPath().getPath()));
		this.members = members;
	}

	private static String[] createArray(String[] members, String... otherStrings) {
		String[] array = new String[otherStrings.length + members.length];
		System.arraycopy(otherStrings, 0, array, 0, otherStrings.length);
		System.arraycopy(members, 0, array, otherStrings.length, members.length);
		return array;
	}

	public void execute() {
		try {
			super.executeCommand();
			// todo verifier que le unlock s'est bien passé
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}
	}
}
