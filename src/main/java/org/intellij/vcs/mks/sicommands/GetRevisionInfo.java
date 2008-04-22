package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * If this command is called for a file that is not a mks member branchTip,memberRev and workingRev will be null
 *
 * @author Thibaut Fagart
 */
public class GetRevisionInfo extends SiCLICommand {
    public static final String COMMAND = "rlog";

    public static final String patternString = revisionPattern + "(?:\\s+)" + revisionPattern + "(?:\\s+)" + revisionPattern; // 1 : branchtip, 2 : member, 3 : working
    private final Pattern pattern = Pattern.compile(patternString);
    private VcsRevisionNumber branchTip = null;
    private VcsRevisionNumber memberRev = null;
    private VcsRevisionNumber workingRev = null;
    private final String memberPath;
    @NonNls
    public static final String NOT_A_MEMBER = "Not a member";

    public GetRevisionInfo(List<VcsException> errors, MksCLIConfiguration mksCLIConfiguration, String memberPath, File directory) {
        super(errors, mksCLIConfiguration, COMMAND, "--noheaderformat", "--notrailerformat", "--fields=branchtiprev,memberrev,workingrev", memberPath);
        this.memberPath = memberPath;
        setWorkingDir(directory);
    }

    @Override
    public void execute() {
        try {
            executeCommand();
            String[] lines = commandOutput.split("\n");
            int start = 0;
            while (shouldIgnore(lines[start])) {
                // skipping connecting/reconnecting lines
                start++;
            }
            // only top line seems to be interesting, we're not interested in the history
            String line = lines[start];
            if (line.trim().length() == 0) {
                return;
            }
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                branchTip = MksRevisionNumber.createRevision(matcher.group(1));
                memberRev = MksRevisionNumber.createRevision(matcher.group(2));
                workingRev = MksRevisionNumber.createRevision(matcher.group(3));
            } else if (line.contains("is not a current or destined or pending member")) {
                LOGGER.warn(this + " not a mks member (any more?) " + memberPath);
            } else {
                LOGGER.error(this + " unexpected command output {" + line + "}, expected (" + patternString + ")");
            }
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        } catch (VcsException e) {
            errors.add(e);
        }
    }

    protected void handleErrorOutput(String errorOutput) {
        if (exitValue == 128 && errorOutput.contains("is not a current or destined or pending member")) {
            errors.add(new VcsException(NOT_A_MEMBER));
            return;
        } else {
            super.handleErrorOutput(errorOutput);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    /**
     * beware : branch tip is the FILE branch, not the development path
     *
     * @return revision number
     */
    public VcsRevisionNumber getBranchTip() {
        return branchTip;
    }

    /**
     * member rev ON CURRENT THE DEVELOPMENT PATH
     *
     * @return revision number
     */
    public VcsRevisionNumber getMemberRev() {
        return memberRev;
    }

    public VcsRevisionNumber getWorkingRev() {
        return workingRev;
    }

    @Override
    public String toString() {
        return "GetRevisionInfo[" + memberPath + "]";
    }
}
