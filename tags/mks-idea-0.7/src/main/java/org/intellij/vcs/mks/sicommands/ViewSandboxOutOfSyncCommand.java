package org.intellij.vcs.mks.sicommands;

import java.util.List;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.model.MksMemberState;
import com.intellij.openapi.vcs.VcsException;

/**
 * Obtains member revision, working revision, checkedout state (won't see
 * modified without checkout) for all members of a sandbox.
 *
 * @author Thibaut Fagart
 */
public class ViewSandboxOutOfSyncCommand extends AbstractViewSandboxCommand {


    public ViewSandboxOutOfSyncCommand(final List<VcsException> errors, final EncodingProvider encodingProvider,
                                       final String sandboxPath) {
        super(errors, encodingProvider, sandboxPath, "--filter=changed:sync", "--filter=!changed:working");
    }

    @Override
    protected MksMemberState createState(final String workingRev, final String memberRev, final String workingCpid,
                                         final String locker, final String lockedSandbox, final String type, final String deferred) throws VcsException {
        return new MksMemberState((MksRevisionNumber.createRevision(workingRev)), (MksRevisionNumber.createRevision(memberRev)), workingCpid,
                isDropped(type) ? MksMemberState.Status.DROPPED : MksMemberState.Status.SYNC);
    }

}
