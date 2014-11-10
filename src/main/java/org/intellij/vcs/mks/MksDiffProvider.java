package org.intellij.vcs.mks;

import java.util.ArrayList;

import org.intellij.vcs.mks.model.MksServerInfo;
import org.intellij.vcs.mks.sicommands.cli.GetRevisionInfo;
import org.intellij.vcs.mks.sicommands.cli.SiCLICommand;
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
        GetRevisionInfo command = getRevisionInfo(virtualFile, errors, 0);
        if (errors.isEmpty()) {
            return command.getWorkingRev();
        } else {
            LOGGER.warn("error occurred org.intellij.vcs.mks.MksDiffProvider.getCurrentRevision");
            return null;
        }
    }

    private GetRevisionInfo getRevisionInfo(@NotNull final VirtualFile virtualFile, final ArrayList<VcsException> errors, int retryCount) {
		GetRevisionInfo command = new GetRevisionInfo(errors, mksVcs, virtualFile.getPath(),
            VfsUtil.virtualToIoFile(virtualFile.getParent()));
        command.execute();
        if (command.errors.isEmpty()) {
            return command;
        } else //noinspection ThrowableResultOfMethodCallIgnored
            if (errors.size() == 1 && errors.get(0).getMessage().equals(GetRevisionInfo.NOT_A_MEMBER)) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        Messages.showMessageDialog("Not (or not any more) a member", "title", Messages.getInformationIcon());
                    }
                };
                MksVcs.invokeLaterOnEventDispatchThread(runnable);
                return command;
            } else if (errors.size() == 1 && errors.get(0).getMessage().equals(SiCLICommand.UNABLE_TO_RECONNECT_TO_MKS_SERVER) && (retryCount == 0)) {
				MKSAPIHelper.getInstance().reconnect(mksVcs.getProject(), MksServerInfo.fromHostAndPort(mksVcs.getSandboxCache().getSandboxInfo(virtualFile).hostAndPort));
				errors.remove(0);
				return getRevisionInfo(virtualFile, errors, retryCount + 1);
			} else {
				LOGGER.warn("error occurred retrieving version info for " + virtualFile.getPresentableName());
				return command;
			}
	}

    @Nullable
    public ItemLatestState getLastRevision(@NotNull final VirtualFile virtualFile) {
        ArrayList<VcsException> errors = new ArrayList<VcsException>();
        GetRevisionInfo command = getRevisionInfo(virtualFile, errors, 0);
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
            final Runnable runnable = new Runnable() {
                public void run() {
                    Messages.showWarningDialog("This revision is not mks controlled", "Error");
                }
            };
            try {
                MksVcs.invokeOnEventDispatchThreadAndWait(runnable);
            } catch (VcsException e) {
                LOGGER.error(e.getCause());
            }
            return null;
        }
        return new MksContentRevision(mksVcs,
            VcsContextFactory.SERVICE.getInstance().createFilePathOn(virtualFile), vcsRevisionNumber);
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
