package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thibaut Fagart
 */
public class ListServers extends SiCLICommand {
    private static final String patternString = "([^@]+)@([^:]+):(\\d+).*";

    public ArrayList<MksServerInfo> servers;

    public static final String COMMAND = "servers";

    public ListServers(List<VcsException> errors, MksCLIConfiguration mksCLIConfiguration) {
        super(errors, mksCLIConfiguration, COMMAND);
    }

    @Override
    public void execute() {
        Pattern pattern = Pattern.compile(patternString);
        ArrayList<MksServerInfo> tempServers = new ArrayList<MksServerInfo>();
        try {
            executeCommand();
            String[] lines = commandOutput.split("\n");
            int start = 0;
            for (int i = start, max = lines.length; i < max; i++) {
                String line = lines[i];
                if (shouldIgnore(line)) {
                    continue;
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String user = matcher.group(1);
                    String host = matcher.group(2);
                    String port = matcher.group(3);
                    tempServers.add(new MksServerInfo(user, host, port));
                } else {
                    LOGGER.error("unexpected command output {" + line + "}, expected [user@host:port]");
                    //noinspection ThrowableInstanceNeverThrown
                    errors.add(new VcsException("ListServers : unexpected line structure " + line));
                }
            }
            servers = tempServers;
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
    }

    protected boolean shouldIgnore(String line) {
        return super.shouldIgnore(line) || line.trim().length() == 0;
    }

    @Override
    public String toString() {
        return "ListServers";
    }

}
