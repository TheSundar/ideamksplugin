package org.intellij.vcs.mks;

import java.util.ArrayList;
import org.intellij.vcs.mks.sicommands.GetContentRevision;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;

/**
 * @author Thibaut Fagart
 */
public class MksContentRevision implements ContentRevision {

    final MksRevisionNumber myRevision;
    private MksVcs mksvcs;
    final FilePath myFile;

    public MksContentRevision(MksVcs mksvcs, FilePath myFile, MksRevisionNumber myRevision) {
        this.mksvcs = mksvcs;
        this.myFile = myFile;
        this.myRevision = myRevision;
    }

    @Nullable
    public String getContent() throws VcsException {
        GetContentRevision getRevisionCommand = new GetContentRevision(new ArrayList<VcsException>(), mksvcs,
            (MksRevisionNumber) this.getRevisionNumber(), this.getFile().getPath());
        getRevisionCommand.execute();
        return getRevisionCommand.getContent();
    }

    @NotNull
    public FilePath getFile() {
        return myFile;
    }

    @NotNull
    public VcsRevisionNumber getRevisionNumber() {
        return myRevision;
    }
}
