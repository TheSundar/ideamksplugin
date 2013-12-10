package org.intellij.vcs.mks.history;

import java.io.IOException;
import java.util.Date;
import org.intellij.vcs.mks.MksContentRevision;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;

public class MksVcsFileRevision implements VcsFileRevision {
	private String author;
	private String commitMessage;
	private Date revisionDate;
	private final MksVcs mksvcs;
	private final FilePath myFile;
	private final VcsRevisionNumber revision;
	private String myContent;
	private final String cpid;

	public MksVcsFileRevision(MksVcs mksvcs, FilePath myFile, MksMemberRevisionInfo info) {
		this.mksvcs = mksvcs;
		this.myFile = myFile;

		author = info.getAuthor();
		commitMessage = info.getDescription();
		revisionDate = info.getDate();
		revision = info.getRevision();
		cpid = info.getCpid();
	}

	public String getAuthor() {
		return author;
	}

	public String getBranchName() {
		return null;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

	public synchronized void loadContent() throws VcsException {

		if (myContent == null) {
			myContent = new MksContentRevision(mksvcs, myFile, revision).getContent();
		}
	}

	public VcsRevisionNumber getRevisionNumber() {
		return revision;
	}

	public byte[] getContent() throws IOException {
		return myContent.getBytes();
	}

	public FilePath getMyFile() {
		return myFile;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MksVcsFileRevision that = (MksVcsFileRevision) o;

		return myFile.equals(that.myFile)
				&& !(revision == null ? that.revision != null : !revision.equals(that.revision));

	}

	@Override
	public int hashCode() {
		int result;
		result = myFile.hashCode();
		result = 31 * result + (revision != null ? revision.hashCode() : 0);
		return result;
	}

	public String toString() {
		return getClass().getSimpleName() + "[" + myFile + "," + revision.asString() + "]";
	}

	public String getCpid() {
		return cpid;
	}
}
