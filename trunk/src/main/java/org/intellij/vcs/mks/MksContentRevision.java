package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.intellij.vcs.mks.sicommands.cli.GetContentRevision;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author Thibaut Fagart
 */
public class MksContentRevision implements ContentRevision {

	final VcsRevisionNumber myRevision;
	private MksVcs mksvcs;
	final FilePath myFile;

	public MksContentRevision(@NotNull MksVcs mksvcs, @NotNull FilePath myFile, @NotNull VcsRevisionNumber myRevision) {
		this.mksvcs = mksvcs;
		this.myFile = myFile;
		this.myRevision = myRevision;
	}

	@Nullable
	public String getContent() throws VcsException {
		if (VcsRevisionNumber.NULL.equals(myRevision)) {
			return null;
		} else {
			GetContentRevision getRevisionCommand = new GetContentRevision(new ArrayList<VcsException>(), mksvcs,
					this.getRevisionNumber(), this.getFile().getPath());
			getRevisionCommand.execute();
			return getRevisionCommand.getContent();
		}
	}

	@NotNull
	public FilePath getFile() {
		return myFile;
	}

	@NotNull
	public VcsRevisionNumber getRevisionNumber() {
		return myRevision;
	}

	@Override
	public String toString() {
		return "MksContentRevision[" + getFile() + ":" + getRevisionNumber() + "]";
	}
}
