package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * This command allows to lock a varying number of members.
 * All the members must belong to the same sandbox or the command will fail
 */
public class LockMemberCommand extends SiCLICommand {
	@NotNull
	private final String[] members;
	@org.jetbrains.annotations.NonNls
	public static final String COMMAND = "lock";

	public LockMemberCommand(List<VcsException> errors, EncodingProvider encodingProvider, @NotNull MksChangePackage changePackage, String... members) {
		super(errors, encodingProvider, COMMAND, createArray("--nobranch", "--nobranchvariant", "--cpid=" + changePackage.getId(), members));
		assert members.length > 0 : "need to specify which member to lock";
		this.members = members;
	}

	private static String[] createArray(String s, String s1, String s2, String[] members) {
		String[] array = new String[3 + members.length];
		array[0] = s;
		array[1] = s1;
		array[2] = s2;
		System.arraycopy(members, 0, array, 3, members.length);
		return array;
	}

	public void execute() {
		try {
			super.executeCommand();
			// todo verifier que le lock s'est bien passé
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}
	}
}
