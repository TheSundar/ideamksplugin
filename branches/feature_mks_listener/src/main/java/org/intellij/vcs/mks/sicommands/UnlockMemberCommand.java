package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.EncodingProvider;
import org.jetbrains.annotations.NotNull;

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

	public UnlockMemberCommand(List<VcsException> errors, EncodingProvider encodingProvider, String... members) {
		super(errors, encodingProvider, COMMAND, createArray("--nobreaklock", members));
		assert members.length > 0 : "need to specify which member to lock";
		this.members = members;
	}

	private static String[] createArray(String s, String[] members) {
		String[] array = new String[1 + members.length];
		array[0] = s;
		System.arraycopy(members, 0, array, 1, members.length);
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
