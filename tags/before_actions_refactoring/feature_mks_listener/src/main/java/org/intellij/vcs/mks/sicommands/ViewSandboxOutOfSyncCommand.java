package org.intellij.vcs.mks.sicommands;

import java.util.List;
import org.intellij.vcs.mks.EncodingProvider;
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
	                                 final String username, final String sandboxPath) {
		super(errors, encodingProvider, username, sandboxPath,/* "--filter=changed",*/"--filter=changed:sync");
	}

	@Override
	protected MksMemberState createState(final String workingRev, final String memberRev, final String workingCpid,
	                                  final String locker, final String lockedSandbox, final String type, final String deferred) throws VcsException {
		return new MksMemberState(createRevision(workingRev), createRevision(memberRev), workingCpid,
			(DROPPED_TYPE.equals(type)?MksMemberState.Status.DROPPED:MksMemberState.Status.SYNC));
	}

}