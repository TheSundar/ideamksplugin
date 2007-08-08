package org.intellij.vcs.mks.sicommands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import org.intellij.vcs.mks.AbstractMKSCommand;
import org.intellij.vcs.mks.EncodingProvider;
import com.intellij.openapi.vcs.VcsException;

/**
 * @author Thibaut Fagart
 */
public abstract class SiCLICommand extends AbstractMKSCommand {
    protected final EncodingProvider encodingProvider;
    private String command;
    private String[] args;
    protected String commandOutput;
    private File workingDir;

    public SiCLICommand(List<VcsException> errors, EncodingProvider encodingProvider, String command, String... args) {
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
            buf.append(s);
            buf.append(" ");
        }
        LOGGER.info("executing " + buf.toString());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        InputStream is = process.getInputStream();
        InputStreamReader reader = new InputStreamReader(is, encodingProvider.getMksSiEncoding(command));
        StringWriter sw;
        try {
            char[] buffer = new char[512];
            int readChars;
            sw = new StringWriter();
            while ((readChars = reader.read(buffer)) != -1) {
                sw.write(new String(buffer, 0, readChars));
            }
        } finally {
            reader.close();
        }
        commandOutput = sw.toString();
        return buf.toString();
    }

    protected boolean shoudIgnore(String line) {
        return line.startsWith("Reconnecting") || line.startsWith("Connecting");
    }
}