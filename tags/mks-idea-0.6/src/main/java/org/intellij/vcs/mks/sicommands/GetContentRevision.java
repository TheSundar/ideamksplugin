package org.intellij.vcs.mks.sicommands;

import java.io.IOException;
import java.util.List;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.jetbrains.annotations.NonNls;
import com.intellij.openapi.vcs.VcsException;

/**
 * @author Thibaut Fagart
 */
public class GetContentRevision extends SiCLICommand {
    @NonNls
    public static final String COMMAND = "viewrevision";
    private final String member;
    private final MksRevisionNumber revisionNumber;

    public GetContentRevision(List<VcsException> errors, EncodingProvider encodingProvider, final MksRevisionNumber revisionNumber, final String path) {
        super(errors, encodingProvider, COMMAND, "-r", revisionNumber.asString(), path);
        this.member = path;
        this.revisionNumber = revisionNumber;
    }

    @Override
    public void execute() {
        try {
            executeCommand();
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
    }

    public String getContent() throws VcsException {
        if (foundError()) {
            for (VcsException vcsException : errors.subList(previousErrorCount, errors.size())) {
                LOGGER.error(vcsException);
            }
            throw new VcsException(errors.get(previousErrorCount));
        } else {
            return commandOutput;
        }
    }
    @Override
    public String toString() {
        return "GetContentRevision["+member+","+revisionNumber.asString()+"]";
    }
}
