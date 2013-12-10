package org.intellij.vcs.mks.realtime;

import junit.framework.TestCase;
import org.intellij.vcs.mks.sicommands.SandboxesCommand;
import org.intellij.vcs.mks.sicommands.SandboxesCommandTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thibaut Fagart
 */
public class SandboxListSynchronizerImplTest extends TestCase {

    public void testCompareRemoved() {

        final SandboxesCommand command = new SandboxesCommandTest().executeCommand(SandboxesCommandTest.FILE_1);
        final ArrayList<SandboxesCommand.SandboxInfo> oldList = command.result;

        final ArrayList<SandboxesCommand.SandboxInfo> newList = new ArrayList<SandboxesCommand.SandboxInfo>(oldList);

        newList.remove(0);
        newList.remove(2);
        final SandboxListSynchronizerImpl synchronizer = new SandboxListSynchronizerImpl(null);

        final ArrayList<SandboxListListener> listeners = new ArrayList<SandboxListListener>();
        final List<String> sandboxAdded = new ArrayList<String>();
        final List<String> sandboxRemoved = new ArrayList<String>();
        final List<String> sandboxUpdated = new ArrayList<String>();
        listeners.add(new SandboxListListener() {

            public void addSandboxPath(@NotNull String sandboxPath, @NotNull String serverHostAndPort, @NotNull String mksProject, @Nullable String devPath, boolean isSubSandbox) {
                sandboxAdded.add(sandboxPath);
            }

            public void removeSandboxPath(@NotNull String sandboxPath, boolean isSubSandbox) {
                sandboxRemoved.add(sandboxPath);
            }

            public void updateSandboxPath(@NotNull String sandboxPath, @NotNull String serverHostAndPort, @NotNull String mksProject, @Nullable String devPath, boolean isSubSandbox) {
                sandboxUpdated.add(sandboxPath);
            }
        });
        synchronizer.compareAndFireUpdates(oldList, newList, listeners);
        assertEquals(2, sandboxRemoved.size());
        assertEquals(oldList.get(0).sandboxPath, sandboxRemoved.get(0));
        assertEquals(oldList.get(3).sandboxPath, sandboxRemoved.get(1));
        assertTrue(sandboxUpdated.isEmpty());
        assertTrue(sandboxAdded.isEmpty());
    }

    public void testCompareUpdated() {

        final SandboxesCommand command = new SandboxesCommandTest().executeCommand(SandboxesCommandTest.FILE_1);
        final ArrayList<SandboxesCommand.SandboxInfo> oldList = command.result;

        final ArrayList<SandboxesCommand.SandboxInfo> newList = new ArrayList<SandboxesCommand.SandboxInfo>(oldList);
        final SandboxesCommand.SandboxInfo sandboxInfo = oldList.get(2);
        final SandboxesCommand.SandboxInfo newSandboxInfo = new SandboxesCommand.SandboxInfo(sandboxInfo.sandboxPath, sandboxInfo.serverHostAndPort, sandboxInfo.projectPath, "newDevpath");
        newList.set(2, newSandboxInfo);
        final SandboxListSynchronizerImpl synchronizer = new SandboxListSynchronizerImpl(null);

        final ArrayList<SandboxListListener> listeners = new ArrayList<SandboxListListener>();
        final List<String> sandboxAdded = new ArrayList<String>();
        final List<String> sandboxRemoved = new ArrayList<String>();
        final List<String> sandboxUpdated = new ArrayList<String>();
        listeners.add(new SandboxListListener() {

            public void addSandboxPath(@NotNull String sandboxPath, @NotNull String serverHostAndPort, @NotNull String mksProject, @Nullable String devPath, boolean isSubSandbox) {
                sandboxAdded.add(sandboxPath);
            }

            public void removeSandboxPath(@NotNull String sandboxPath, boolean isSubSandbox) {
                sandboxRemoved.add(sandboxPath);
            }

            public void updateSandboxPath(@NotNull String sandboxPath, @NotNull String serverHostAndPort, @NotNull String mksProject, @Nullable String devPath, boolean isSubSandbox) {
                sandboxUpdated.add(sandboxPath);
            }
        });
        synchronizer.compareAndFireUpdates(oldList, newList, listeners);
        assertEquals(1, sandboxUpdated.size());
        assertEquals(newSandboxInfo.sandboxPath, sandboxUpdated.get(0));
        assertTrue(sandboxRemoved.isEmpty());
        assertTrue(sandboxAdded.isEmpty());
    }

    public void testCompareAdded() {

        final SandboxesCommand command = new SandboxesCommandTest().executeCommand(SandboxesCommandTest.FILE_1);
        final ArrayList<SandboxesCommand.SandboxInfo> oldList = command.result;

        final ArrayList<SandboxesCommand.SandboxInfo> newList = new ArrayList<SandboxesCommand.SandboxInfo>(oldList);
        final SandboxesCommand.SandboxInfo sandboxInfo = oldList.get(oldList.size() - 1);

        final SandboxesCommand.SandboxInfo newSandboxInfo = new SandboxesCommand.SandboxInfo(sandboxInfo.sandboxPath.replace("c:", "d:"), sandboxInfo.serverHostAndPort, sandboxInfo.projectPath, sandboxInfo.projectVersion);
        newList.add(newSandboxInfo);
        final SandboxListSynchronizerImpl synchronizer = new SandboxListSynchronizerImpl(null);

        final ArrayList<SandboxListListener> listeners = new ArrayList<SandboxListListener>();
        final List<String> sandboxAdded = new ArrayList<String>();
        final List<String> sandboxRemoved = new ArrayList<String>();
        final List<String> sandboxUpdated = new ArrayList<String>();
        listeners.add(new SandboxListListener() {

            public void addSandboxPath(@NotNull String sandboxPath, @NotNull String serverHostAndPort, @NotNull String mksProject, @Nullable String devPath, boolean isSubSandbox) {
                sandboxAdded.add(sandboxPath);
            }

            public void removeSandboxPath(@NotNull String sandboxPath, boolean isSubSandbox) {
                sandboxRemoved.add(sandboxPath);
            }

            public void updateSandboxPath(@NotNull String sandboxPath, @NotNull String serverHostAndPort, @NotNull String mksProject, @Nullable String devPath, boolean isSubSandbox) {
                sandboxUpdated.add(sandboxPath);
            }
        });
        synchronizer.compareAndFireUpdates(oldList, newList, listeners);
        assertEquals(1, sandboxAdded.size());
        assertEquals(newSandboxInfo.sandboxPath, sandboxAdded.get(0));
        assertTrue(sandboxRemoved.isEmpty());
        assertTrue(sandboxUpdated.isEmpty());
    }

}
