package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;
import org.intellij.vcs.mks.CommandExecutionListener;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.sicommands.api.ListSandboxesAPI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListSandboxesAPITest extends TestCase {
    private static final String ENCODING = "IBM437";
    public static final String FILE_1 = "/org/intellij/vcs/mks/realtime/sandboxlist.properties";

    private ListSandboxesAPI createCommand(final List<VcsException> errors, final MKSAPIHelper helper) {

        MksCLIConfiguration mksCLIConfiguration = new MksCLIConfiguration() {
            @NotNull
            public String getMksSiEncoding(final String command) {
                return ENCODING;
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
        return new ListSandboxesAPI(errors, mksCLIConfiguration) {
            @Override
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
        ListSandboxesAPI command;
        try {
            command = createCommand(errors, helper);
            command.execute();
        } finally {
            helper.disposeComponent();
        }
        for (String sandbox : command.sandboxes) {
            System.out.println(sandbox);
        }

    }

}
