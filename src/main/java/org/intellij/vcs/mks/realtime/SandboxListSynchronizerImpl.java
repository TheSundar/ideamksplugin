package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksConfiguration;
import org.intellij.vcs.mks.sicommands.ListSandboxes;
import org.intellij.vcs.mks.sicommands.SandboxesCommand;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * starts a si sandboxes --persist and listens to updates but doesn't actually use the output.
 * Whenever a new update is detected, issues a si sandboxes (without --persist), compares the output to
 * the previous state and issues events accordingly to any registered listeners.
 * <p/>
 * A listener being registered while comparison is in progress should get the new list
 *
 * @author Thibaut Fagart
 */
public class SandboxListSynchronizerImpl extends AbstractMKSSynchronizer
        implements ApplicationComponent, SandboxListSynchronizer {
    private final ArrayList<SandboxListListener> listeners = new ArrayList<SandboxListListener>();
    //	private static final String LINE_SEPARATOR = " -> ";
    private static final String MKS_PROJECT_PJ = "project.pj";


    private ArrayList<SandboxesCommand.SandboxInfo> currentList = new ArrayList<SandboxesCommand.SandboxInfo>();
    private final ReentrantLock sandboxCacheLock = new ReentrantLock();

    public SandboxListSynchronizerImpl() {
        this(ApplicationManager.getApplication().getComponent(MksConfiguration.class));
    }

    protected SandboxListSynchronizerImpl(MksConfiguration config) {
        super(ListSandboxes.COMMAND, config, "--displaySubs");
    }

    public void addListener(@NotNull SandboxListListener listener) {
        if (this.listeners.contains(listener)) {
            return;
        }
        sandboxCacheLock.lock();
        try {
            this.listeners.add(listener);
            for (SandboxesCommand.SandboxInfo sandbox : currentList) {
                listener.addSandboxPath(sandbox.sandboxPath, sandbox.serverHostAndPort, sandbox.projectPath,
                        sandbox.projectVersion, sandbox.subSandbox);
            }
        } finally {
            sandboxCacheLock.unlock();
        }
    }

    public void removeListener(@NotNull SandboxListListener sandboxListListener) {
        this.listeners.remove(sandboxListListener);
    }


    @NonNls
    @NotNull
    public String getComponentName() {
        return "MKS.sandboxListSaynchronizer";
    }

    public void initComponent() {
        MKSHelper.startClient();
        addIgnoredFiles();
        start();
    }

    public void disposeComponent() {
        stop();
    }

    private static void addIgnoredFiles() {
        String patterns = FileTypeManager.getInstance().getIgnoredFilesList();

        StringBuffer newPattern = new StringBuffer(patterns);
        if (patterns.indexOf(MKS_PROJECT_PJ) == -1) {
            newPattern.append((newPattern.charAt(newPattern.length() - 1) == ';') ? "" : ";").append(MKS_PROJECT_PJ);
        }

        final String newPatternString = newPattern.toString();
        if (!newPatternString.equals(patterns)) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    FileTypeManager.getInstance().setIgnoredFilesList(newPatternString);
                }
            }
            );
        }
    }

    @Override
    protected void handleLine(String line) {
        try {
            if (shoudIgnore(line)) {
                return;
            }
            if (line.startsWith("-----")) {
                // detection of a new update
                LOGGER.debug("update notification : " + line);
                updateSandboxList();
            }
        } catch (Exception e) {
            LOGGER.error("error parsing mks synchronizer output [" + line + "], skipping that line  because : " +
                    e.getMessage(), e);
        }
    }

    private void updateSandboxList() {
        this.sandboxCacheLock.lock();
        ArrayList<SandboxesCommand.SandboxInfo> oldList;
        final ArrayList<SandboxListListener> listeners;
        ArrayList<SandboxesCommand.SandboxInfo> newSandboxList;
        try {
            newSandboxList = getNewSandboxList();
            listeners = this.listeners;
            oldList = this.currentList;
            this.currentList = newSandboxList;
        } finally {
            this.sandboxCacheLock.unlock();
        }

        compareAndFireUpdates(oldList, newSandboxList, listeners);
    }

    /**
     * compares the 2 (sorted) lists of sandbox and fire updates appropriately to the provided list of listeners
     *
     * @param oldList
     * @param newList
     * @param listeners
     */
    protected void compareAndFireUpdates(ArrayList<SandboxesCommand.SandboxInfo> oldList, ArrayList<SandboxesCommand.SandboxInfo> newList, ArrayList<SandboxListListener> listeners) {
        int oldIndex = 0;
        int newIndex = 0;

        while (oldIndex < oldList.size() && newIndex < newList.size()) {
            final SandboxesCommand.SandboxInfo oldSandbox = oldList.get(oldIndex);
            final SandboxesCommand.SandboxInfo newSandbox = newList.get(newIndex);

            final int compareCode = oldSandbox.sandboxPath.compareTo(newSandbox.sandboxPath);
            if (0 == compareCode) {
                if (!oldSandbox.equals(newSandbox)) {
                    fireSandboxUpdated(newSandbox, listeners);
                }
                oldIndex++;
                newIndex++;
            } else if (compareCode < 0) {
                fireSandboxRemoved(oldSandbox, listeners);
                oldIndex++;
            } else {
                fireSandboxAdded(newSandbox, listeners);
                newIndex++;
            }
        }

        while (oldIndex < oldList.size()) {
            fireSandboxRemoved(oldList.get(oldIndex), listeners);
            oldIndex++;
        }
        while (newIndex < newList.size()) {
            fireSandboxAdded(newList.get(newIndex), listeners);
            newIndex++;
        }
    }


    private void fireSandboxAdded(SandboxesCommand.SandboxInfo sandbox, ArrayList<SandboxListListener> listeners) {
        for (SandboxListListener listener : listeners) {
            listener.addSandboxPath(sandbox.sandboxPath, sandbox.serverHostAndPort, sandbox.projectPath,
                    sandbox.projectVersion, sandbox.subSandbox);
        }
    }

    private void fireSandboxRemoved(SandboxesCommand.SandboxInfo sandbox, ArrayList<SandboxListListener> listeners) {
        for (SandboxListListener listener : listeners) {
            listener.removeSandboxPath(sandbox.sandboxPath, sandbox.subSandbox);
        }
    }

    private void fireSandboxUpdated(SandboxesCommand.SandboxInfo sandbox, ArrayList<SandboxListListener> listeners) {
        for (SandboxListListener listener : listeners) {
            listener.updateSandboxPath(sandbox.sandboxPath, sandbox.serverHostAndPort, sandbox.projectPath,
                    sandbox.projectVersion, sandbox.subSandbox);
        }
    }

    public String getDescription() {
        return "sandbox list listener";
    }

    protected ArrayList<SandboxesCommand.SandboxInfo> getNewSandboxList() {
        final SandboxesCommand command = new SandboxesCommand(new ArrayList<VcsException>(),
                ApplicationManager.getApplication().getComponent(MksConfiguration.class));
        return command.result;
    }
}
