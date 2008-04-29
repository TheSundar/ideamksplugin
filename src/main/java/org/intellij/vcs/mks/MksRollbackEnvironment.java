package org.intellij.vcs.mks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.rollback.DefaultRollbackEnvironment;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
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
	 *
	 * @param changes changes to be rolled back
	 * @return a list of exceptions
	 */
	public List<VcsException> rollbackChanges(List<Change> changes) {
		// we can use "si revert ${change.getBeforeRevision().getFile()}" this will revert the file to the
		// project member revision
		// todo but it could be that the revision we should revert to is not the member revision ?

		final List<VcsException> exceptions = new ArrayList<VcsException>();

		for (Change change : changes) {
			File beforePath = null;
			logger.debug("MksRollbackEnvironment.rollbackChanges " + change);
			ContentRevision beforeRevision = change.getBeforeRevision();
			if (beforeRevision != null && beforeRevision instanceof MksContentRevision) {
				// file was not created by change
				beforePath = beforeRevision.getFile().getIOFile();
				revert(beforePath, exceptions);
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
                        exceptions.add(new VcsException(e));
                    }
                    checkRevertFile(afterPath, exceptions);
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

		return exceptions;

	}

	/**
	 * reverts the file using "si revert"
	 *
	 * @param file	   the IO file to be reverted
	 * @param exceptions holder for any exceptions occuring
	 */
	private void revert(File file, List<VcsException> exceptions) {
		logger.debug("MksRollbackEnvironment.revert " + file);
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
	 *
	 * @param files
	 * @return
	 */
	public List<VcsException> rollbackMissingFileDeletion(List<FilePath> files) {

		final List<VcsException> exceptions = new ArrayList<VcsException>();
		for (FilePath filePath : files) {
			// todo
			logger.debug("MksRollbackEnvironment.rollbackMissingFileDeletion " + filePath);
		}
		return exceptions;
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
	public List<VcsException> rollbackModifiedWithoutCheckout(List<VirtualFile> files) {
		final List<VcsException> exceptions = new ArrayList<VcsException>();
		for (VirtualFile file : files) {
			revert(VfsUtil.virtualToIoFile(file), exceptions);
			// todo
			logger.debug("MksRollbackEnvironment.rollbackModifiedWithoutCheckout " + file);
		}
		return exceptions;
	}
}
