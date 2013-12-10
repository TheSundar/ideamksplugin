package org.intellij.vcs.mks.sicommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.intellij.vcs.mks.EncodingProvider;
import com.intellij.openapi.vcs.VcsException;

/**
 * @author Thibaut Fagart
 */
public class ListSandboxes extends SiCLICommand {
    private static final String LINE_SEPARATOR = " -> ";
    public ArrayList<String> sandboxes;
    public static final String COMMAND = "sandboxes";

    public ListSandboxes(List<VcsException> errors, EncodingProvider encodingProvider) {
        super(errors, encodingProvider, COMMAND, "--displaySubs");
    }

    @Override
    public void execute() {
        ArrayList<String> tempSandboxes = new ArrayList<String>();
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
                String[] parts = line.split(LINE_SEPARATOR);
                if (parts.length < 2) {
                    LOGGER.error("unexpected command output {" + line + "}, expected 2 parts separated by [" + LINE_SEPARATOR + "]", "");
                    //noinspection ThrowableInstanceNeverThrown
                    errors.add(new VcsException("unexpected line structure " + line));
                } else {
                    tempSandboxes.add(parts[0]);
                }
            }
            sandboxes = tempSandboxes;
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
    }

    @Override
    public String toString() {
        return "ListSandboxes";
    }

}
