package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.MksCLIConfiguration;
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
    public static final String COMMAND = "viewsandbox";
    protected static final String DROPPED_TYPE = "dropped";

    private static final String fieldsParam;
    protected static final String wholeLinePatternString;
    //	protected static final String typePattern = "((?:sandbox)|(?:subsandbox)|(?:shared-subsandbox)|(?:shared-variant-subsandbox)" +
    //			"|(?:shared-build-subsandbox)|(?:member)|(?:archived)|(?:dropped)|(?:variant-subsandbox)" +
    //			"|(?:" + DEFERRED_ADD + ")|(?:" + DEFERRED_DROP + "))";
    protected static final String typePattern = "([^\\s]+)";

    static {
        // --fields=workingrev,memberrev,workingcpid,deferred,pendingcpid,revsyncdelta,type,wfdelta,name
        fieldsParam = "--fields=locker,workingrev,workingcpid,deferred,type,name,memberrev,locksandbox";
        wholeLinePatternString = "^" + userPattern
                + " " + revisionPattern + " " + changePackageIdPattern
                + " " + deferredPattern + " " + typePattern
                + " " + namePattern
                + " " + revisionPattern + " " + sandboxPattern + "$"; // locked sandbox is null when member is not locked
    }

    private static final int LOCKER_GROUP_IDX = 1;
    private static final int WORKING_REV_GROUP_IDX = LOCKER_GROUP_IDX + 1;
    private static final int WORKING_CPID_GROUP_IDX = WORKING_REV_GROUP_IDX + 1;
    private static final int DEFERRED_GROUP_IDX = WORKING_CPID_GROUP_IDX + 1;
    private static final int TYPE_GROUP_IDX = DEFERRED_GROUP_IDX + 1;
    private static final int NAME_GROUP_IDX = TYPE_GROUP_IDX + 1;
    private static final int MEMBER_REV_GROUP_IDX = NAME_GROUP_IDX + 1;
    private static final int LOCKED_SANDBOX_GROUP_IDX = MEMBER_REV_GROUP_IDX + 1;

    /**
     * Map<FilePath.getPath(), MksMemberState>
     * records the state as far as mks knows it for every file returned by the command
     */
    protected final Map<String, MksMemberState> memberStates = new HashMap<String, MksMemberState>();
    protected final String sandboxPath;

    protected AbstractViewSandboxCommand(final List<VcsException> errors, final MksCLIConfiguration mksCLIConfiguration,
                                         final String sandboxPath, final String... filters) {
        super(errors, mksCLIConfiguration, COMMAND, createParams(fieldsParam, "--recurse", "--sandbox=" + sandboxPath, filters));
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
                        if (isRelevant(type)) {
                            MksMemberState memberState = createState(workingRev, memberRev, workingCpid, locker, lockedSandbox, type, deferred);
                            setState(name, memberState);
                        } else {
                            LOGGER.debug("ignoring " + line);
//						} else {
//							LOGGER.warn("unexpected type " + type + " for " + line);
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

    protected boolean isRelevant(final String type) {
        return isMember(type);
    }

    private boolean isSandbox(final String type) {
        return type.contains("sandbox");
    }

    /**
     * Associates a member state with its file name.
     * made protected to be overriden for unit tests as VcsUtil is not available at that time
     *
     * @param name        the absolute file name
     * @param memberState the state of the file
     */
    protected void setState(@NotNull final String name, @NotNull final MksMemberState memberState) {
        memberStates.put(VcsUtil.getFilePath(name).getPath(), memberState);
    }

    private boolean isMember(final String type) {
        return !isSandbox(type) && !type.contains("subproject");
    }

    public Map<String, MksMemberState> getMemberStates() {
        return Collections.unmodifiableMap(memberStates);
    }

    protected boolean isDropped(String type) {
        return DROPPED_TYPE.equals(type) || DEFERRED_DROP.equals(type);
    }
}
