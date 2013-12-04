package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.*;
import com.mks.api.response.APIException;
import com.mks.api.response.InvalidCommandSelectionException;
import com.mks.api.response.Response;
import org.intellij.vcs.mks.AbstractMKSCommand;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SiAPICommand extends AbstractMKSCommand {


	protected SiAPICommand(@NotNull List<VcsException> errors, @NotNull String command, @NotNull MksCLIConfiguration mksCLIConfiguration) {
		super(errors, command, mksCLIConfiguration);
	}


    protected MKSAPIHelper getAPIHelper() {
        return MKSAPIHelper.getInstance();
    }

    protected Response executeCommand(Command command) throws APIException {
        CmdRunner cmdRunner= null;
        Session session= null;
        long start = System.currentTimeMillis();
        try {
//            LOGGER.warn("executing " + command);
            String[] strings = command.toStringArray();
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < strings.length-1; i++) {
                buf.append(strings[i]).append(" ");
            }
            if (!strings[strings.length-1].endsWith("--")) {
                buf.append(strings[strings.length - 1]);
            }
            LOGGER.warn("executing " + buf.toString());
            session = getAPIHelper().getSession();
            cmdRunner = session.createCmdRunner();
            return cmdRunner.execute(command);
        } finally {
//            System.err.println("MKS end "+this.command);
            try {
                if (cmdRunner != null) {
                    cmdRunner.release();
                }
            } catch (APIException e) {
            }
            fireCommandCompleted(start);

        }
    }
}
