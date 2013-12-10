package org.intellij.vcs.mks.sicommands;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.MksRevisionNumber;
import com.intellij.openapi.vcs.VcsException;

/**
 * @author Thibaut Fagart
 */
public class GetRevisionInfo extends SiCLICommand {
    public static final String COMMAND = "rlog";
    public static final String patternString = "([\\d\\.]*)(?:\\s+)([\\d\\.]*)(?:\\s+)([\\d\\.]*)"; // 1 : branchtip, 2 : member, 3 : working
    private final Pattern pattern = Pattern.compile(patternString);
    private MksRevisionNumber branchTip;
    private MksRevisionNumber memberRev;
    private MksRevisionNumber workingRev;
    private final String memberPath;

    public GetRevisionInfo(List<VcsException> errors, EncodingProvider encodingProvider, String memberPath, File directory) {
        super(errors, encodingProvider, COMMAND, "--noheaderformat", "--notrailerformat", "--fields=branchtiprev,memberrev,workingrev", memberPath);
        this.memberPath = memberPath;
        setWorkingDir(directory);
    }

    @Override
    public void execute() {
        try {
            executeCommand();
            String[] lines = commandOutput.split("\n");
            int start = 0;
            while (shoudIgnore(lines[start])) {
                // skipping connecting/reconnecting lines
                start++;
            }
            // only top line seems to be interesting, we're not interested in the history
            String line = lines[start];
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                branchTip = new MksRevisionNumber(matcher.group(1));
                memberRev = new MksRevisionNumber(matcher.group(2));
                workingRev = new MksRevisionNumber(matcher.group(3));
            } else {
                LOGGER.error("unexpected command output {" + line + "}, expected (" + patternString + ")");
            }
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        } catch (VcsException e) {
            errors.add(e);
        }
    }

    /**
     * beware : branch tip is the FILE branch, not the development path
     *
     * @return revision number
     */
    public MksRevisionNumber getBranchTip() {
        return branchTip;
    }

    /**
     * member rev ON CURRENT THE DEVELOPMENT PATH
     *
     * @return revision number
     */
    public MksRevisionNumber getMemberRev() {
        return memberRev;
    }

    public MksRevisionNumber getWorkingRev() {
        return workingRev;
    }
    @Override
    public String toString() {
        return "GetRevisionInfo[" + memberPath + "]";
    }

}
