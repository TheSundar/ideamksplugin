package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thibaut Fagart
 */
public abstract class AbstractViewSandboxCommand extends SiCLICommand {
	protected static final String DEFERRED = "deferred";
	private static final String COMMAND = "viewsandbox";
	protected static final String DROPPED_TYPE = "dropped";

	private static final String fieldsParam;
	protected static final String wholeLinePatternString;
	//	protected static final String typePattern = "((?:sandbox)|(?:subsandbox)|(?:shared-subsandbox)|(?:shared-variant-subsandbox)" +
	//			"|(?:shared-build-subsandbox)|(?:member)|(?:archived)|(?:dropped)|(?:variant-subsandbox)" +
	//			"|(?:" + DEFERRED_ADD + ")|(?:" + DEFERRED_DROP + "))";
	protected static final String typePattern = "([^\\s]+)";

	static {
		// --fields=workingrev,memberrev,workingcpid,deferred,pendingcpid,revsyncdelta,type,wfdelta,name
		fieldsParam = "--fields=workingrev,workingcpid,deferred,pendingcpid,type,locker,name,memberrev,locksandbox";
		wholeLinePatternString = "^" + revisionPattern + " " + changePackageIdPattern
				+ " " + deferredPattern + " " + unusedPattern
				+ " " + typePattern
				+ " " + userPattern
				+ " " + namePattern
				+ " " + revisionPattern + " " + sandboxPattern + "$"; // locked sandbox is null when member is not locked
	}

	private static final int WORKING_REV_GROUP_IDX = 1;
	private static final int WORKING_CPID_GROUP_IDX = 2;
	private static final int DEFERRED_GROUP_IDX = 3;
	private static final int TYPE_GROUP_IDX = 5;
	private static final int LOCKER_GROUP_IDX = 6;
	private static final int NAME_GROUP_IDX = 7;
	private static final int MEMBER_REV_GROUP_IDX = 8;
	private static final int LOCKED_SANDBOX_GROUP_IDX = 9;

	/**
	 * Map<FilePath.getPath(), MksMemberState>
	 * records the state as far as mks knows it for every file returned by the command
	 */
	protected final Map<String, MksMemberState> memberStates = new HashMap<String, MksMemberState>();
	protected final String sandboxPath;

	protected AbstractViewSandboxCommand(final List<VcsException> errors, final EncodingProvider encodingProvider,
										 final String sandboxPath, final String... filters) {
		super(errors, encodingProvider, COMMAND, createParams(fieldsParam, "--recurse", "--sandbox=" + sandboxPath, filters));
		this.sandboxPath = sandboxPath;
	}

	private static String[] createParams(final String fieldsParam, final String s, final String s1, final String[] filters) {
		String[] params = new String[3 + filters.length];
		params[0] = fieldsParam;
		params[1] = s;
		params[2] = s1;
		System.arraycopy(filters, 0, params, 3, filters.length);
		return params;
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
//			System.out.println("pattern [" + wholeLinePatternString + "]");
		Pattern wholeLinePattern = Pattern.compile(wholeLinePatternString);
		BufferedReader reader = new BufferedReader(new StringReader(commandOutput));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				Matcher matcher = wholeLinePattern.matcher(line);
				if (matcher.matches()) {
					String workingRev = matcher.group(WORKING_REV_GROUP_IDX);
					String memberRev = matcher.group(MEMBER_REV_GROUP_IDX);
					String workingCpid = matcher.group(WORKING_CPID_GROUP_IDX);
					String deferred = matcher.group(DEFERRED_GROUP_IDX);
					String type = matcher.group(TYPE_GROUP_IDX);
					String locker = matcher.group(LOCKER_GROUP_IDX);
					String name = matcher.group(NAME_GROUP_IDX);
					String lockedSandbox = matcher.group(LOCKED_SANDBOX_GROUP_IDX);
					try {
						if (isMember(type)) {
							MksMemberState memberState = createState(workingRev, memberRev, workingCpid, locker, lockedSandbox, type, deferred);
							setState(name, memberState);
						} else if (isSandbox(type)) {
							LOGGER.debug("ignoring sandbox " + name);
						} else {
							LOGGER.warn("unexpected type " + type + " for " + line);
						}
					} catch (VcsException e) {
						// should not happen, VcsExceptions on ChangePackageId
						if (e.getCause() != null) {
							LOGGER.error(e);
						}
						//noinspection ThrowableInstanceNeverThrown
						errors.add(new VcsException(name + " " + e.getMessage()));

					}
				} else {
					//noinspection ThrowableInstanceNeverThrown
					errors.add(new VcsException(toString() + " : unexpected line [" + line + "]"));
				}
			}
		} catch (IOException e) {
			// shouldnt happen :IO exceptions on stringReader.read
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}

	}

	protected abstract MksMemberState createState(String workingRev, String memberRev, String workingCpid, final String locker, final String lockedSandbox, final String type, final String deferred) throws VcsException;

	private boolean isSandbox(final String type) {
		return type.contains("sandbox");
	}

	/**
	 * Associates a member state with its file name.
	 * made protected to be overriden for unit tests as VcsUtil is not available at that time
	 *
	 * @param name		the absolute file name
	 * @param memberState the state of the file
	 */
	protected void setState(@NotNull final String name, @NotNull final MksMemberState memberState) {
		memberStates.put(VcsUtil.getFilePath(name).getPath(), memberState);
	}

	private boolean isMember(final String type) {
		return !isSandbox(type);
	}

	public Map<String, MksMemberState> getMemberStates() {
		return Collections.unmodifiableMap(memberStates);
	}

	protected boolean isDropped(String type) {
		return DROPPED_TYPE.equals(type) || DEFERRED_DROP.equals(type);
	}
}
