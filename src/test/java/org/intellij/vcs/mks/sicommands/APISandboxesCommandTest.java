package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;
import org.intellij.vcs.mks.CommandExecutionListener;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.sicommands.api.SandboxesCommandAPI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class APISandboxesCommandTest extends TestCase {
    private SandboxesCommandAPI createCommand(final List<VcsException> errors, final MKSAPIHelper helper) {

        MksCLIConfiguration mksCLIConfiguration = new MksCLIConfiguration() {
            @NotNull
            public String getMksSiEncoding(final String command) {
                return null;
            }

            @NotNull
            public String getDatePattern() {
                return "MMM dd, yyyy - hh:mm a";
            }

            public CommandExecutionListener getCommandExecutionListener() {
                return CommandExecutionListener.IDLE;
            }

            public boolean isMks2007() {
                return false;
            }
            @Override
            public Locale getDateLocale() {
                return Locale.ENGLISH;
            }

        };
        return new SandboxesCommandAPI(errors, mksCLIConfiguration) {
            protected MKSAPIHelper getAPIHelper() {
                return helper;
            }

        };

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testIt() {
        ArrayList<VcsException> errors = new ArrayList<VcsException>();

        MKSAPIHelper helper = new MKSAPIHelper();
        helper.initComponent();
        SandboxesCommandAPI command;
        try {
            command = createCommand(errors, helper);
            command.execute();
        } finally {
            helper.disposeComponent();
        }
        for (SandboxInfo sandboxInfo : command.result) {
            System.out.println(sandboxInfo);
        }
    }
}
