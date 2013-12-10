package org.intellij.vcs.mks.sicommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.vcs.VcsException;

/**
 * @author Thibaut Fagart
 */
public class SiConnectCommand extends SiCLICommand {
    private final String host;
    private final String port;
    private final String user;
    private MksServerInfo server;
    public static final String COMMAND = "connect";

    public SiConnectCommand(@NotNull MksCLIConfiguration mksCLIConfiguration, @NotNull String host,
        @NotNull String port,
        @NotNull String user, @NotNull String password) {
        super(new ArrayList<VcsException>(), mksCLIConfiguration, COMMAND, "--hostname=" + host, "--port=" + port,
            "--user=" + user, "--password=" + password);
        this.host = host;
        this.port = port;
        this.user = user;
    }

    public void execute() {
        try {
            super.executeCommand();
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
        if (exitValue == 0) {
            server = new MksServerInfo(user, host, port);
        }
    }

    /**
     * override because otherwise the user's password may end up in log files !
     *
     * @return
     */
    @Override
    public String toString() {
        String toString = super.toString();
        String patternString = "--password=[^\\s]+";
        final Pattern pattern = Pattern.compile(patternString);
        final String[] strings = pattern.split(toString);
        StringBuffer buf = new StringBuffer(toString.length());
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            buf.append(string);
            if (i + 1 < strings.length) {
                buf.append("--password=<password>");
            }
        }
        toString = buf.toString();
        return toString;
    }

    public MksServerInfo getServer() {
        return server;
    }
}
