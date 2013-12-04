package org.intellij.vcs.mks.sicommands.cli;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * uses si editcp --summary=newName --hostname=host. <br/>
 * This command writes its "normal output" to stderr ... in this case, the id of the changepackage affected
 *
 * @author Thibaut Fagart
 */
public class RenameChangePackage extends SiCLICommand {
	@NotNull
	private final MksChangePackage changePackage;
	@org.jetbrains.annotations.NonNls
	public static final String COMMAND = "editcp";

	public RenameChangePackage(@NotNull List<VcsException> errors, @NotNull MksVcs mksvcs,
							   @NotNull MksChangePackage changePackage, @NotNull String newName) {
		super(errors, mksvcs, COMMAND, "--summary=" + newName, "--hostname=" + changePackage.server,
				changePackage.getId());
		this.changePackage = changePackage;
	}

	@Override
	public void execute() {
		try {
			executeCommand();
			BufferedReader reader = new BufferedReader(new StringReader(commandOutput));
			String line;
			while (((line = reader.readLine()) != null) && shouldIgnore(line)) {
			}
			if (null != line) {
				String message = "unexpected command output {" + commandOutput + "}, expected nothing";
				LOGGER.error(message);
				//noinspection ThrowableInstanceNeverThrown
				errors.add(new VcsException(message));
			}
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}
	}

	@Override
	protected void handleErrorOutput(String errorOutput) {
		if (!isErrorCode(exitValue)) {
			try {
				BufferedReader reader = new BufferedReader(new StringReader(errorOutput));
				String line;
				while (((line = reader.readLine()) != null) && shouldIgnore(line)) {
				}

				if (line == null || !line.startsWith(changePackage.getId())) {
					String message = "unexpected command error output {" + errorOutput + "}, expected {" +
							changePackage.getId() + "}";
					LOGGER.error(message);
					//noinspection ThrowableInstanceNeverThrown
					errors.add(new VcsException(message));
				}
				while (((line = reader.readLine()) != null) && shouldIgnore(line)) {
				}
				if (line != null) {
					String message = "unexpected command error output {" + errorOutput + "}, expected {" +
							changePackage.getId() + "}";
					LOGGER.error(message);
					//noinspection ThrowableInstanceNeverThrown
					errors.add(new VcsException(message));
				}
			} catch (IOException e) {
				//noinspection ThrowableInstanceNeverThrown
				errors.add(new VcsException(e));
			}
		} else {
			super.handleErrorOutput(errorOutput);
		}
	}

	protected boolean isErrorCode(int exitValue) {
		return !(0 == exitValue);
	}

	@Override
	public String toString() {
		return "RenameChangePackage[" + changePackage.getId() + "]";
	}

}
