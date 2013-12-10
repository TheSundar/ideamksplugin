package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksMemberState;

import java.util.List;

/**
 * Obtains member revision, working revision, checkedout state (won't see
 * modified without checkout) for all members of a sandbox.
 *
 * @author Thibaut Fagart
 */
public class ViewSandboxMissingCommand extends AbstractViewSandboxCommand {


	public ViewSandboxMissingCommand(final List<VcsException> errors, final EncodingProvider encodingProvider,
									 final String sandboxPath) {
		super(errors, encodingProvider, sandboxPath,/* "--filter=changed",*/"--filter=changed:missing");
	}

	@Override
	protected MksMemberState createState(final String workingRev, final String memberRev, final String workingCpid, final String locker, final String lockedSandbox, final String type, final String deferred) throws VcsException {
		if (isDropped(type)) {
			return new MksMemberState(createRevision(workingRev), createRevision(memberRev), workingCpid,
					MksMemberState.Status.DROPPED);
		} else {
			return new MksMemberState(createRevision(workingRev), createRevision(memberRev), workingCpid,
					MksMemberState.Status.MISSISNG);
		}
	}

}