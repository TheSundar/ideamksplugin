package org.intellij.vcs.mks;

import java.util.StringTokenizer;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;

/**
 * @author Thibaut Fagart
 */
public class MksRevisionNumber implements VcsRevisionNumber {
	private final String revision;
	private final int[] parts;

	public static VcsRevisionNumber createRevision(@Nullable String revAsString) throws VcsException {
		return (revAsString == null) ?
				VcsRevisionNumber.NULL :
				new MksRevisionNumber(revAsString);

	}

	MksRevisionNumber(String revision) throws VcsException {
		this.revision = revision;
		String[] stringParts = revision.split("\\.");
		parts = new int[stringParts.length];
		try {
			for (int i = 0, max = parts.length; i < max; i++) {
				parts[i] = Integer.parseInt(stringParts[i]);
			}
		} catch (NumberFormatException e) {
			throw new VcsException("invalid mks revision number, expects \\d+(.\\d+)*, got " + revision);
		}
	}

	public String asString() {
		return revision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MksRevisionNumber that = (MksRevisionNumber) o;

		return revision.equals(that.revision);

	}

	@Override
	public int hashCode() {
		return revision.hashCode();
	}

	/**
	 * @return the parent revision of of this revision
	 */
	public String getParentRevision() {
		StringTokenizer tok = new StringTokenizer(revision, ".");
		int[] tokens = new int[tok.countTokens()];
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = Integer.parseInt(tok.nextToken());
		}

		assert tokens.length % 2 == 0 : "expecting a non-odd number of tokens";
		StringBuffer parentRevBuf = new StringBuffer(revision.length());
		if (tokens[tokens.length - 1] != 1) {
			// parent version is in the same branch, decrement last token
			for (int i = 0; i < tokens.length - 1; i++) {
				int token = tokens[i];
				parentRevBuf.append(token).append('.');
			}
			parentRevBuf.append(tokens[tokens.length - 1] - 1);
		} else {
			// this is start of a branch, drop the last 2 tokens
			for (int i = 0; i < tokens.length - 2; i++) {
				int token = tokens[i];
				parentRevBuf.append(token);
				if (i < tokens.length - 3) {
					parentRevBuf.append('.');
				}
			}
		}
		return parentRevBuf.toString();
	}

	public int compareTo(VcsRevisionNumber other) {
		if (other == VcsRevisionNumber.NULL) {
			return 1;
		} else if (!(other instanceof MksRevisionNumber)) {
			throw new IllegalArgumentException();
		}
		MksRevisionNumber mksOther = (MksRevisionNumber) other;
		for (int i = 0, max = parts.length, maxOther = mksOther.parts.length; i < max; i++) {
			if (i < maxOther) {
				if (parts[i] != mksOther.parts[i]) {
					return new Integer(parts[i]).compareTo(mksOther.parts[i]);
				}
			} else {
				return +1;
			}
		}
		if (mksOther.parts.length > parts.length) {
			return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + asString() + "]";
	}
}
