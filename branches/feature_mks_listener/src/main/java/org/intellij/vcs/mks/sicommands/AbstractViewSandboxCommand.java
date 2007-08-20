package org.intellij.vcs.mks.sicommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;

/**
 * @author Thibaut Fagart
 */
public abstract class AbstractViewSandboxCommand extends SiCLICommand {
	private final Logger LOGGER = Logger.getInstance(getClass().getName());

	private static final String COMMAND = "viewsandbox";
	private static final String revisionPattern = "([\\d\\.]+)?";
	private static final String changePackageIdPattern = "([\\d:]+)?";
	private static final String typePattern = "((?:sandbox)|(?:subsandbox)|(?:shared-subsandbox)|(?:shared-variant-subsandbox)|(?:shared-build-subsandbox)|(?:member)|(?:archived)|(?:dropped)|(?:variant-subsandbox))";
	private static final String unusedPattern = "([^\\s]+)?";
	private static final String namePattern = "(.+)";
	private static final String sandboxPattern =namePattern+"?";
	private static final String userPattern = "([^\\s]+)?";
	protected static final String DROPPED_TYPE = "dropped";

	private static final String fieldsParam;
	protected static final String wholeLinePatternString;

	static {
		// --fields=workingrev,memberrev,workingcpid,deferred,pendingcpid,revsyncdelta,type,wfdelta,name
		fieldsParam = "--fields=workingrev,workingcpid,deferred,pendingcpid,type,locker,name,memberrev,locksandbox";
		wholeLinePatternString = "^" + revisionPattern + " " + changePackageIdPattern
			+ " " + unusedPattern + " " + unusedPattern
			+ " " + typePattern
			+ " " + userPattern
			+ " " + namePattern
			+ " " + revisionPattern + " " + sandboxPattern + "?$"; // locked sandbox is null when member is not locked
	}

	private static final int WORKING_REV_GROUP_IDX = 1;
	private static final int WORKING_CPID_GROUP_IDX = 2;
	private static final int TYPE_GROUP_IDX = 5;
	private static final int LOCKER_GROUP_IDX = 6;
	private static final int NAME_GROUP_IDX = 7;
	private static final int MEMBER_REV_GROUP_IDX = 8;
	private static final int LOCKED_SANDBOX_GROUP_IDX = 9;

	protected final Map<String, MksMemberState> memberStates = new HashMap<String, MksMemberState>();
	protected final String mksUsername;
	protected final String sandboxPath;

	protected AbstractViewSandboxCommand(final List<VcsException> errors, final EncodingProvider encodingProvider,
	                                     String mksUsername, final String sandboxPath, final String... filters) {
		super(errors, encodingProvider, COMMAND, createParams(fieldsParam, "--recurse", "--sandbox="+sandboxPath , filters));
		this.mksUsername = mksUsername;
		this.sandboxPath = sandboxPath;
	}

	private static String[] createParams(final String fieldsParam, final String s, final String s1, final String[] filters) {
		String [] params = new String[3+filters.length];
		params[0] = fieldsParam;
		params[1] = s;
		params[2] = s1;
		System.arraycopy(filters, 0, params, 3,filters.length);
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
				try {
					Matcher matcher = wholeLinePattern.matcher(line);
					if (matcher.matches()) {
						String workingRev = matcher.group(WORKING_REV_GROUP_IDX);
						String memberRev = matcher.group(MEMBER_REV_GROUP_IDX);
						String workingCpid = matcher.group(WORKING_CPID_GROUP_IDX);
						String type = matcher.group(TYPE_GROUP_IDX);
						String locker = matcher.group(LOCKER_GROUP_IDX);
						String name = matcher.group(NAME_GROUP_IDX);
						String lockedSandbox = matcher.group(LOCKED_SANDBOX_GROUP_IDX);
						if (isMember(type)) {
							MksMemberState memberState = createState(workingRev, memberRev, workingCpid, locker, lockedSandbox, type);
							setState(name, memberState);
						} else if (isSandbox(type)) {
							LOGGER.debug("ignoring sandbox " + name);
						} else {
							LOGGER.warn("unexpected type " + type + " for " + line);
						}
					} else {
						//noinspection ThrowableInstanceNeverThrown
						errors.add(new VcsException("unexpected line [" + line + "]"));
					}
				} catch (VcsException e) {
					// should not happen, VcsExceptions on ChangePackageId
					//noinspection ThrowableInstanceNeverThrown
					errors.add(new VcsException(e));

				}
			}
		} catch (IOException e) {
			// shouldnt happen :IO exceptions on stringReader.read
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}

	}

	protected abstract MksMemberState createState(String workingRev, String memberRev, String workingCpid, final String locker, final String lockedSandbox, final String type) throws VcsException;

	private boolean isSandbox(final String type) {
		return type.contains("sandbox");
	}

	private void setState(final String name, final MksMemberState memberState) {
		memberStates.put(name, memberState);
	}

	private boolean isMember(final String type) {
		return !isSandbox(type);
	}

	public Map<String, MksMemberState> getMemberStates() {
		return Collections.unmodifiableMap(memberStates);
	}

	/**
	 * returns null if type is DROPPED (means there is no member rev)
	 * @param memberRev
	 * @param type
	 * @return null or a valid MksRevisionNumber
	 * @throws VcsException
	 */
	@Nullable
	protected MksRevisionNumber createMemberRev(final String memberRev, final String type) throws VcsException {
		return DROPPED_TYPE.equals(type) ? null : new MksRevisionNumber(memberRev);
	}
}
