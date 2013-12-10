package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;

/**
 * @author Thibaut Fagart
 */
public class MksRevisionNumber implements VcsRevisionNumber {
    private final String revision;
    private final int[] parts;

    public MksRevisionNumber(String revision) throws VcsException {
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

    public int compareTo(VcsRevisionNumber other) {
        if (!(other instanceof MksRevisionNumber)) {
            throw new IllegalArgumentException();
        }
        MksRevisionNumber mksOther = (MksRevisionNumber) other;
        for (int i = 0, max = parts.length, maxOther = mksOther.parts.length; i < max; i++)
        {
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
