package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.intellij.vcs.mks.AbstractMKSCommand;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.io.AsyncStreamBuffer;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Thibaut Fagart
 */
public abstract class SiCLICommand extends AbstractMKSCommand implements Runnable {
	protected static final String DEFERRED_ADD = "deferred-add";
	protected static final String DEFERRED_DROP = "deferred-drop";
	protected static final String DEFERRED_CHECKIN = "deferred-check-in";
	protected static final String revisionPattern = "(\\d+(?:\\.\\d+)*)?";
	protected static final String changePackageIdPattern = "(\\d+:\\d+)?";
	protected static final String deferredPattern = "(deferred)?";
	protected static final String unusedPattern = "([^\\s]+)?";
	protected static final String namePattern = "(.+)";
	protected static final String sandboxPattern = namePattern + "?";
	protected static final String userPattern = "(?:(?:[^\\t]+? \\()?([^\\)]+)(?:\\))?)?";
	protected static final String DEFERRED = "deferred";
	private String commandString;
	private String[] args;
	protected String commandOutput;
	private File workingDir;
	protected int exitValue;
	private static final int SI_IDX = 0;
	private static final int COMMAND_IDX = 1;
	private static final int BATCH_IDX = 2;
	private boolean batchMode;

	public SiCLICommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration,
						@NotNull String command, @NonNls String... args) {
		this(errors, mksCLIConfiguration, command, true, args);
	}

	public SiCLICommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration,
						@NotNull String command, boolean batch, @NonNls String... args) {
		super(errors, command, mksCLIConfiguration);
		this.batchMode = batch;
		this.args = args;
	}

	public void addArg(String arg) {
		String[] newArgs = new String[args.length + COMMAND_IDX];
		System.arraycopy(args, SI_IDX, newArgs, SI_IDX, args.length);
		newArgs[args.length] = arg;
		args = newArgs;
	}

	public void setWorkingDir(File aDir) {
		workingDir = aDir;
	}

	protected String executeCommand() throws IOException {
		String[] processArgs = createCommand();
		ProcessBuilder builder = new ProcessBuilder(processArgs);
		if (workingDir != null) {
			builder.directory(workingDir);
		}
		StringBuffer buf = new StringBuffer();
		for (String s : builder.command()) {
			boolean surroundWithQuotes = s.contains(" ");
			if (surroundWithQuotes) {
				buf.append("\"");
			}
			buf.append(s);
			if (surroundWithQuotes) {
				buf.append("\"");
			}
			buf.append(" ");
		}
		long start = System.currentTimeMillis();
		commandString = buf.toString();
		logCommand(buf);
		builder.redirectErrorStream(false);
		Process process = builder.start();
		try {
			AsyncStreamBuffer stderr =
					new AsyncStreamBuffer(process.getErrorStream());
			AsyncStreamBuffer stdout =
					new AsyncStreamBuffer(process.getInputStream());

			commandOutput = new String(stdout.get(), mksCLIConfiguration.getMksSiEncoding(command));
			String errorOutput = new String(stderr.get(), mksCLIConfiguration.getMksSiEncoding(command));
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				LOGGER.debug("interrupted", e);
			}
			handleErrorOutput(errorOutput);
		} finally {
			fireCommandCompleted(start);
		}
		return buf.toString();
	}

	protected void logCommand(StringBuffer buf) {
		LOGGER.warn("executing " + buf.toString());
	}


	private String[] createCommand() {
		final int implicitArgCount = (batchMode) ? BATCH_IDX + 1 : BATCH_IDX;
		String[] processArgs = new String[args.length + implicitArgCount];
		processArgs[SI_IDX] = "si";
		processArgs[COMMAND_IDX] = command;
		if (batchMode) {
			processArgs[BATCH_IDX] = "--batch";
		}
		System.arraycopy(args, SI_IDX, processArgs, implicitArgCount, args.length);
		return processArgs;
	}

	protected void handleErrorOutput(String errorOutput) {
		if (!"".equals(errorOutput)) {
			if (exitValue == SI_IDX) {
				LOGGER.warn("command [" + this + "] wrote to stderr " + errorOutput);
			} else if (exitValue == BATCH_IDX && errorOutput.startsWith("Connecting to ")) {
				LOGGER.warn("mks returned [" + errorOutput +
						"], you probably need to reconnect to the server manually, try executing 'si connect --hostname=$mksHost$'");
			} else {
				LOGGER.error("return code " + exitValue + " for command " + this
						+ ", stdErr=" + errorOutput);
			}
		}
	}

	protected boolean shouldIgnore(String line) {
		return line.startsWith("Reconnecting") || line.startsWith("Connecting");
	}

	public void run() {
		execute();
	}

	/**
	 * @param revision the revision number as obtained from the MKS CLI
	 * @return VcsRevisionNumber.NULL if no revision is applicable or a valid
	 *         MksRevisionNumber
	 * @throws com.intellij.openapi.vcs.VcsException
	 *          if the revision doesn't follow MKS conventions \d+(\.\d+)*
	 */
	@Nullable
	protected VcsRevisionNumber createRevision(final String revision) throws VcsException {
		return MksRevisionNumber.createRevision(revision);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + commandString + "]";
	}

	protected boolean isDeferred(String deferred) {
		return DEFERRED.equals(deferred);
	}

	protected MksMemberState createDeferredState(String workingRev, String memberRev, String workingCpid,
												 String type) throws VcsException {
		return createDeferredState(workingRev, memberRev, workingCpid, type, null);
	}

	protected MksMemberState createDeferredState(String workingRev, String memberRev, String workingCpid, String type,
												 Date memberTimestamp) throws VcsException {
		if (DEFERRED_ADD.equals(type)) {
			return new MksMemberState((MksRevisionNumber.createRevision(workingRev)), null, workingCpid,
					MksMemberState.Status.ADDED, memberTimestamp);
		} else if (DEFERRED_DROP.equals(type)) {
			return new MksMemberState(null, (MksRevisionNumber.createRevision(memberRev)), workingCpid,
					MksMemberState.Status.DROPPED, memberTimestamp);
		} else if (DEFERRED_CHECKIN.equals(type)) {
			return new MksMemberState(null, (MksRevisionNumber.createRevision(memberRev)), workingCpid,
					MksMemberState.Status.CHECKED_OUT, memberTimestamp);
		} else {
			LOGGER.warn(this + " : deferred operation (" + type + ") not supported at moment, returning 'unknown'");
			return new MksMemberState((MksRevisionNumber.createRevision(workingRev)),
					(MksRevisionNumber.createRevision(memberRev)), workingCpid,
					MksMemberState.Status.UNKNOWN, memberTimestamp);
		}
	}
}
