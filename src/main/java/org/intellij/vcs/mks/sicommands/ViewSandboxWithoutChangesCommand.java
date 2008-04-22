package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksMemberState;

import java.util.List;

/**
 * Obtains members that HAVE NOT BEEN CHANGED LOCALLY.
 * Deferred members won't be included either
 *
 * @author Thibaut Fagart
 */
public class ViewSandboxWithoutChangesCommand extends AbstractViewSandboxCommand {


    public ViewSandboxWithoutChangesCommand(final List<VcsException> errors, final MksCLIConfiguration mksCLIConfiguration, final String sandboxPath) {
        super(errors, mksCLIConfiguration, sandboxPath, "--filter=!changed", "--filter=!deferred");
    }

    @Override
    protected MksMemberState createState(final String workingRev, final String memberRev, final String workingCpid,
                                         final String locker, final String lockedSandbox, final String type, final String deferred) throws VcsException {
        VcsRevisionNumber workingRevision = createRevision(workingRev);
        VcsRevisionNumber memberRevision = createRevision(memberRev);
        if (!"archived".equals(type)) {
            LOGGER.warn("exepecting only non changed members " + type);
//			return new MksMemberState(workingRevision, memberRevision, workingCpid, MksMemberState.Status.DROPPED);
//		} else if (memberRevision != null && workingRevision == null) {
//			return new MksMemberState(workingRevision, memberRevision, workingCpid, MksMemberState.Status.MISSISNG);
            throw new VcsException("exepecting only non changed members");
        } else {
            return new MksMemberState(workingRevision, memberRevision, workingCpid, MksMemberState.Status.NOT_CHANGED);
        }
    }

    @Override
    protected boolean isRelevant(String type) {
        return super.isRelevant(type) && !isDropped(type);
    }
}
