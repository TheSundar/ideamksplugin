package org.intellij.vcs.mks.sicommands;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.intellij.vcs.mks.AbstractMKSCommand;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.io.AsyncStreamBuffer;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;

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
	protected static final String userPattern = "(?:(?:[^\\t]+? \\()?([^\\(\\s\\)]+)(?:\\))?)?";
	protected static final String DEFERRED = "deferred";
	private String commandString;
	protected final EncodingProvider encodingProvider;
	private String command;
	private String[] args;
	protected String commandOutput;
	private File workingDir;
	protected int exitValue;

	public SiCLICommand(@NotNull List<VcsException> errors, @NotNull EncodingProvider encodingProvider, @NotNull String command, String... args) {
		super(errors);
		this.encodingProvider = encodingProvider;
		this.command = command;
		this.args = args;
	}

	public void setWorkingDir(File aDir) {
		workingDir = aDir;
	}

	protected String executeCommand() throws IOException {
		String[] processArgs = new String[args.length + 3];
		processArgs[0] = "si";
		processArgs[1] = command;
		processArgs[2] = "--batch";
		System.arraycopy(args, 0, processArgs, 3, args.length);
		ProcessBuilder builder = new ProcessBuilder(processArgs);
		if (workingDir != null) {
			builder.directory(workingDir);
		}
		StringBuffer buf = new StringBuffer();
		for (String s : builder.command()) {
			boolean surroundWithQuotes = s.indexOf(' ') >= 0;
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
		LOGGER.warn("executing " + buf.toString());
		builder.redirectErrorStream(false);
		Process process = builder.start();
		try {
			AsyncStreamBuffer stderr =
					new AsyncStreamBuffer(process.getErrorStream());
			AsyncStreamBuffer stdout =
					new AsyncStreamBuffer(process.getInputStream());

			commandOutput = new String(stdout.get(), encodingProvider.getMksSiEncoding(command));
			String errorOutput = new String(stderr.get(), encodingProvider.getMksSiEncoding(command));
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				LOGGER.debug("interrupted", e);
			}
			handleErrorOutput(errorOutput);
		} finally {
			LOGGER.debug(toString() + " finished in " + (System.currentTimeMillis() - start + " ms"));
		}
		return buf.toString();
	}

	protected void handleErrorOutput(String errorOutput) {
		if (!"".equals(errorOutput)) {
			if (exitValue == 0) {
				LOGGER.warn("command [" + this + "] wrote to stderr " + errorOutput);
			} else if (exitValue == 2 && errorOutput.startsWith("Connecting to ")) {
				LOGGER.warn("mks returned [" + errorOutput + "], you probably need to reconnect to the server manually, try executing 'si connect --hostname=$mksHost$'");
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

	protected MksMemberState createDeferredState(String workingRev, String memberRev, String workingCpid, String type) throws VcsException {
		return createDeferredState(workingRev, memberRev, workingCpid, type, null);
	}

	protected MksMemberState createDeferredState(String workingRev, String memberRev, String workingCpid, String type, Date memberTimestamp) throws VcsException {
		if (DEFERRED_ADD.equals(type)) {
			return new MksMemberState((MksRevisionNumber.createRevision(workingRev)), null, workingCpid,
					MksMemberState.Status.ADDED, memberTimestamp);
		} else if (DEFERRED_DROP.equals(type)) {
			return new MksMemberState(null, (MksRevisionNumber.createRevision(memberRev)), workingCpid,
					MksMemberState.Status.DROPPED, memberTimestamp);
		} else if (DEFERRED_CHECKIN.equals(type))  {
			return new MksMemberState(null, (MksRevisionNumber.createRevision(memberRev)), workingCpid,
					MksMemberState.Status.CHECKED_OUT, memberTimestamp);
		} else {
			LOGGER.warn(this + " : deferred operation (" + type + ") not supported at moment, returning 'unknown'");
			return new MksMemberState((MksRevisionNumber.createRevision(workingRev)), (MksRevisionNumber.createRevision(memberRev)), workingCpid,
					MksMemberState.Status.UNKNOWN, memberTimestamp);
		}
	}
}
