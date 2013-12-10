package org.intellij.vcs.mks;

import java.util.ArrayList;
import org.intellij.vcs.mks.sicommands.GetRevisionInfo;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;

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

    private GetRevisionInfo getRevisionInfo(final VirtualFile virtualFile, final ArrayList<VcsException> errors) {
        GetRevisionInfo command = new GetRevisionInfo(errors, mksVcs, virtualFile.getPath(), VfsUtil.virtualToIoFile(virtualFile.getParent()));
        command.execute();
        return command;
    }

    @Nullable
    public VcsRevisionNumber getLastRevision(final VirtualFile virtualFile) {
        ArrayList<VcsException> errors = new ArrayList<VcsException>();
        GetRevisionInfo command = getRevisionInfo(virtualFile, errors);
        if (errors.isEmpty()) {
            return command.getMemberRev();
        } else {
            LOGGER.warn("error occurred org.intellij.vcs.mks.MksDiffProvider.getLastRevision");
            return null;
        }
    }

    @Nullable
    public ContentRevision createFileContent(final VcsRevisionNumber vcsRevisionNumber, final VirtualFile virtualFile) {
        return new MksContentRevision(mksVcs, PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(virtualFile), (MksRevisionNumber) vcsRevisionNumber);
    }
}
