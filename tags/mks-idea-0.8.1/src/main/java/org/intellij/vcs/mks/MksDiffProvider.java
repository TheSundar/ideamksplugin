package org.intellij.vcs.mks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.intellij.vcs.mks.sicommands.GetRevisionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.diff.ItemLatestState;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Thibaut Fagart
 */
public class MksDiffProvider implements DiffProvider {
    private final Logger LOGGER = Logger.getInstance(getClass().getName());
    private final MksVcs mksVcs;

    public MksDiffProvider(final MksVcs mksVcs) {
        this.mksVcs = mksVcs;
    }

    @Nullable
    public VcsRevisionNumber getCurrentRevision(final VirtualFile virtualFile) {
        ArrayList<VcsException> errors = new ArrayList<VcsException>();
        GetRevisionInfo command = getRevisionInfo(virtualFile, errors);
        if (errors.isEmpty()) {
            return command.getWorkingRev();
        } else {
            LOGGER.warn("error occurred org.intellij.vcs.mks.MksDiffProvider.getCurrentRevision");
            return null;
        }
    }

    private GetRevisionInfo getRevisionInfo(@NotNull final VirtualFile virtualFile, final ArrayList<VcsException> errors) {
        GetRevisionInfo command = new GetRevisionInfo(errors, mksVcs, virtualFile.getPath(),
            VfsUtil.virtualToIoFile(virtualFile.getParent()));
        command.execute();
        if (command.errors.isEmpty()) {
            return command;
        } else //noinspection ThrowableResultOfMethodCallIgnored
            if (errors.size() == 1 && errors.get(0).getMessage().equals(GetRevisionInfo.NOT_A_MEMBER)) {
                Messages.showMessageDialog("Not (or not any more) a member", "title", Messages.getInformationIcon());
                return command;
            } else {
                LOGGER.warn("error occurred retrieving version info for " + virtualFile.getPresentableName());
                return command;
            }
    }

    @Nullable
    public ItemLatestState getLastRevision(@NotNull final VirtualFile virtualFile) {
        ArrayList<VcsException> errors = new ArrayList<VcsException>();
        GetRevisionInfo command = getRevisionInfo(virtualFile, errors);
        if (errors.isEmpty()) {
            return new ItemLatestState(command.getMemberRev(), true, false);
        } else {
            LOGGER.warn("error occurred org.intellij.vcs.mks.MksDiffProvider.getLastRevision");
            return null;
        }
    }

    @Nullable
    public ContentRevision createFileContent(final VcsRevisionNumber vcsRevisionNumber, final VirtualFile virtualFile) {
        if (VcsRevisionNumber.NULL.equals(vcsRevisionNumber)) {
            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            showRevisionNotControlledErrorDialog();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (InvocationTargetException e) {
                    LOGGER.error(e.getTargetException());
                }
            } else {
                showRevisionNotControlledErrorDialog();
            }
            return null;
        }
        return new MksContentRevision(mksVcs,
            VcsContextFactory.SERVICE.getInstance().createFilePathOn(virtualFile), vcsRevisionNumber);
    }

    private void showRevisionNotControlledErrorDialog() {
        Messages.showWarningDialog("This revision is not mks controlled", "Error");
    }

    @Override
    @Nullable
    public ItemLatestState getLastRevision(FilePath filePath) {
        final VirtualFile file = filePath.getVirtualFile();
        return (file == null) ? null : getLastRevision(file);
    }

    /**
     * The method is used to check whether there were any new commits under some root. It can return null - than it would mean that
     * there's no information about whether something had changed (result of this method invocation is cached and compared with next
     * invocation's result). <br/> So, return null.
     *
     * @param vcsRoot
     * @return null if revisions are not repository wide
     * @see <a href="http://www.jetbrains.net/devnet/message/5243690#5243690">forum</a>
     */
    @Override
    @Nullable
    public VcsRevisionNumber getLatestCommittedRevision(VirtualFile vcsRoot) {
		return null;
	}
}
