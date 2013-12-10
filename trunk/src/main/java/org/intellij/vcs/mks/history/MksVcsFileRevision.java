package org.intellij.vcs.mks.history;

import com.intellij.openapi.util.Throwable2Computable;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.impl.ContentRevisionCache;
import org.intellij.vcs.mks.MksContentRevision;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Date;

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

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public String getBranchName() {
		return null;
	}

	@Override
	public String getCommitMessage() {
		return commitMessage;
	}

	@Override
	public Date getRevisionDate() {
		return revisionDate;
	}

	@Override
	public synchronized byte[] loadContent() throws VcsException {

		if (myContent == null) {
			myContent = new MksContentRevision(mksvcs, myFile, revision).getContent();
		}
		return myContent.getBytes();
	}

	@Override
	public VcsRevisionNumber getRevisionNumber() {
		return revision;
	}

	@Nullable
	@Override
	public byte[] getContent() throws IOException {
		try {
			return ContentRevisionCache.getOrLoadAsBytes(mksvcs.getProject(), myFile, revision, MksVcs.OUR_KEY, ContentRevisionCache.UniqueType.REPOSITORY_CONTENT,
					new Throwable2Computable<byte[], VcsException, IOException>() {
						@Override
						public byte[] compute() throws VcsException, IOException {
							return loadContent();
						}
					});
		} catch (VcsException e) {
			if (e.getCause() != null && e.getCause() instanceof IOException) {
				throw ((IOException) e.getCause());
			} else {
				e.printStackTrace();
				return null;
			}
		}
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + myFile + "," + revision.asString() + "]";
	}

	public String getCpid() {
		return cpid;
	}

	@Override
	public RepositoryLocation getChangedRepositoryPath() {
		return null;
	}

}
