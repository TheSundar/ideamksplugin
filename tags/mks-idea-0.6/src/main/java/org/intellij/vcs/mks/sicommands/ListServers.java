package org.intellij.vcs.mks.sicommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.intellij.vcs.mks.EncodingProvider;
import com.intellij.openapi.vcs.VcsException;

/**
 * @author Thibaut Fagart
 */
public class ListServers extends SiCLICommand {
    public ArrayList<String> servers;
    public static final String COMMAND = "servers";

    public ListServers(List<VcsException> errors, EncodingProvider encodingProvider) {
        super(errors, encodingProvider, COMMAND);
    }

    @Override
    public void execute() {
        ArrayList<String> tempServers = new ArrayList<String>();
        try {
            executeCommand();
            String[] lines = commandOutput.split("\n");
            int start = 0;
            while (shoudIgnore(lines[start])) {
                // skipping connecting/reconnecting lines
                start++;
            }
            for (int i = start, max = lines.length; i < max; i++) {
                String line = lines[i];
                if (line.contains("@") && line.contains(":")) {
                    int arobaseIndex = line.indexOf('@');
                    String user = line.substring(0, arobaseIndex);
                    String server = line.substring(arobaseIndex + 1, line.indexOf(':'));
                    tempServers.add(server);
                } else {
                    LOGGER.error("unexpected command output {" + line + "}, expected [user@host:port]");
                    //noinspection ThrowableInstanceNeverThrown
                    errors.add(new VcsException("unexpected line structure " + line));
                }
            }
            servers = tempServers;
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
    }
    @Override
    public String toString() {
        return "ListServers";
    }

}
