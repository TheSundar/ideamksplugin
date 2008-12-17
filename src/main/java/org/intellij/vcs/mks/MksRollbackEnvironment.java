package org.intellij.vcs.mks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.rollback.DefaultRollbackEnvironment;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class MksRollbackEnvironment extends DefaultRollbackEnvironment {
    private final Logger logger = Logger.getInstance(MksRollbackEnvironment.class.getName());
    private final MksVcs mksVcs;

    public MksRollbackEnvironment(MksVcs mksVcs) {
        this.mksVcs = mksVcs;
    }

    @Override
    public String getRollbackOperationName() {
        return "Revert";
    }


    /**
     * This is the most common case of rollback: rolling back changes to files which
     * have been checked out.
     */
    public void rollbackChanges(List<Change> changes, List<VcsException> vcsExceptions, @NotNull RollbackProgressListener rollbackProgressListener) {
        // we can use "si revert ${change.getBeforeRevision().getFile()}" this will revert the file to the
        // project member revision
        // todo but it could be that the revision we should revert to is not the member revision ?

        for (Change change : changes) {
            File beforePath = null;
            logger.debug("MksRollbackEnvironment.rollbackChanges " + change);
            ContentRevision beforeRevision = change.getBeforeRevision();
            if (beforeRevision != null && beforeRevision instanceof MksContentRevision) {
                // file was not created by change
                beforePath = beforeRevision.getFile().getIOFile();
                revert(beforePath, vcsExceptions);
            }
            ContentRevision afterRevision = change.getAfterRevision();
            if (afterRevision != null) {
                File afterPath = afterRevision.getFile().getIOFile();
                if (!afterPath.equals(beforePath)) {
                    // todo file has been renamed !!!
/*
                    UnversionedFilesCollector collector = new UnversionedFilesCollector();
                    try {
                        ((SvnChangeProvider) mySvnVcs.getChangeProvider()).getChanges(afterRevision.getFile(), false, collector);
                    }
                    catch (SVNException e) {
                        vcsExceptions.add(new VcsException(e));
                    }
                    checkRevertFile(afterPath, vcsExceptions);
                    // rolling back a rename should delete the after file
                    if (beforePath != null) {
                        for (VirtualFile f : collector.getUnversionedFiles()) {
                            File ioFile = new File(f.getPath());
                            ioFile.renameTo(new File(beforePath, ioFile.getName()));
                        }
                        FileUtil.delete(afterPath);
                    }
*/
                }
            }
        }
    }

    /**
     * reverts the file using "si revert"
     *
     * @param file       the IO file to be reverted
     * @param exceptions holder for any exceptions occuring
     */
    private void revert(File file, List<VcsException> exceptions) {
        logger.debug("MksRollbackEnvironment.revert " + file);
        // si revert --overwriteChanged
        /*
si revert --batch --overwriteUnchanged --overwriteChanged --overwriteDeferred  --restoreTimestamp [(-S sandbox|--sandbox=sandbox)] [(-F file|--selectionFile=file)]


--------------------------------------------------------------------------------

         */
        // todo throw new UnsupportedOperationException("Method revert not yet implemented");
    }

    /**
     * This is called when the user performs an "undo" that returns a file to a
     * state in which it was checked out or last saved. The implementation of this
     * method can compare the current state of the file with the base revision and
     * undo the checkout if the file is identical. Implementing this method is optional.
     *
     * @param file the virtual file to be rolledback
     */
    @Override
    public void rollbackIfUnchanged(@NotNull VirtualFile file) {
        // todo
        logger.debug("MksRollbackEnvironment.rollbackIfUnchanged " + file);
    }

    /**
     * This is called for files reported as "locally deleted": the user has deleted
     * the file locally but not scheduled it for deletion from VCS. The implementation
     * of this method should get the current version of the listed files from the
     * VCS. <br/>
     * You don't need to implement this method if you never report such files to
     * ChangelistBuilder.
     */
    public void rollbackMissingFileDeletion(List<FilePath> filePaths, List<VcsException> vcsExceptions, RollbackProgressListener rollbackProgressListener) {
        for (FilePath filePath : filePaths) {
            // todo
            logger.debug("MksRollbackEnvironment.rollbackMissingFileDeletion " + filePath);
        }
    }

    /**
     * This is called for files reported as "modified without checkout": the user
     * has made a file writable but did not checkout it from the VCS.
     * <p/>
     * You don't need to implement this method if you never report such files to ChangelistBuilder.
     *
     * @param files
     * @return
     */
    @Override
    public void rollbackModifiedWithoutCheckout(List<VirtualFile> files,
                                                List<VcsException> vcsExceptions,
                                                RollbackProgressListener rollbackProgressListener) {
        for (VirtualFile file : files) {
            revert(VfsUtil.virtualToIoFile(file), vcsExceptions);
            // todo
            logger.debug("MksRollbackEnvironment.rollbackModifiedWithoutCheckout " + file);
        }
    }
}
