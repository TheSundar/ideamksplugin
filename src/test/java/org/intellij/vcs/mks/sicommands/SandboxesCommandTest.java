package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SandboxesCommandTest extends TestCase {
    private static final String ENCODING = "IBM437";
    public static final String FILE_1 = "/org/intellij/vcs/mks/realtime/sandboxlist.properties";

    public void test() {
        final SandboxesCommand command = executeCommand(FILE_1);
        assert (command.result.size() == 12);

    }

    public SandboxesCommand executeCommand(String outputFile) {
        final SandboxesCommand command = createCommand(new ArrayList<VcsException>(), outputFile);
        command.execute();
        return command;
    }

    private SandboxesCommand createCommand(final List<VcsException> errors, final String outputFile) {
        MksCLIConfiguration mksCLIConfiguration = new MksCLIConfiguration() {
            @NotNull
            public String getMksSiEncoding(final String command) {
                return ENCODING;
            }

            @NotNull
            public String getDatePattern() {
                return "MMM dd, yyyy - hh:mm a";
            }
        };
        return new SandboxesCommand(errors, mksCLIConfiguration) {
            @Override
            protected String executeCommand() throws IOException {
                commandOutput = loadResourceWithEncoding(outputFile, ENCODING);
                return outputFile;
            }
        };
    }

    private String loadResourceWithEncoding(final String path, final String encoding) throws IOException {
        URL resource = getClass().getResource(path);
        InputStream inputStream = resource.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String line;
        while ((line = reader.readLine()) != null) {
            pw.println(line);
        }
        pw.flush();
        return sw.toString();
    }

}
