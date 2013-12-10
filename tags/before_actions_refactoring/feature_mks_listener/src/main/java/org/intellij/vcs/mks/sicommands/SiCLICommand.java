package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.AbstractMKSCommand;
import org.intellij.vcs.mks.EncodingProvider;

import java.io.*;
import java.util.List;

/**
 * @author Thibaut Fagart
 */
public abstract class SiCLICommand extends AbstractMKSCommand implements Runnable {
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
        long start = System.currentTimeMillis();
        LOGGER.debug("executing " + buf.toString());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        InputStream is = process.getInputStream();
        Reader reader = new BufferedReader(new InputStreamReader(is, encodingProvider.getMksSiEncoding(command)));
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
            try {
                process.exitValue();
            } catch (IllegalThreadStateException e) {
                process.destroy();
            }
            LOGGER.debug(toString() + " finished in " + (System.currentTimeMillis() - start + " ms"));
        }
        commandOutput = sw.toString();
        return buf.toString();
    }

    protected boolean shoudIgnore(String line) {
        return line.startsWith("Reconnecting") || line.startsWith("Connecting");
    }

    public void run() {
        execute();
    }
}
