package org.intellij.vcs.mks.realtime;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.intellij.vcs.mks.CommandExecutionListener;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

public class APISandboxCacheTest extends /*TestCase */LightPlatformCodeInsightFixtureTestCase {
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
    };




    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testIt() {
        MKSAPIHelper helper = new MKSAPIHelper();
        helper.initComponent();

        try {
            APISandboxCache cache = new APISandboxCache(null) {
                @Override
                protected MksCLIConfiguration getConfiguration() {
                    return mksCLIConfiguration;
                }
            };
            cache.dumpStateOn(new PrintWriter(System.out));
        } finally {
            helper.disposeComponent();
        }
    }

}
