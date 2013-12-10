package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.model.MksMemberState;

import java.util.List;

/**
 * does not see files that have been locally deleted, but sees deferred-drops
 *
 * @author Thibaut Fagart
 */
public class ViewSandboxRemoteChangesCommand extends AbstractViewSandboxCommand {


	public ViewSandboxRemoteChangesCommand(final List<VcsException> errors,
										   final MksCLIConfiguration mksCLIConfiguration,
										   final String sandboxPath) {
		super(errors, mksCLIConfiguration, sandboxPath,/* "--filter=changed",*/"--filter=changed:sync,changed:newmem");
	}

	@Override
	protected MksMemberState createState(final String workingRev, final String memberRev, final String workingCpid,
										 final String locker, final String lockedSandbox, final String type,
										 final String deferred) throws VcsException {
		if (isDeferred(deferred)) {
			if (isDropped(type)) {
				return new MksMemberState(null, (MksRevisionNumber.createRevision(memberRev)), workingCpid,
						MksMemberState.Status.DROPPED);
			} else {
				LOGGER.warn("unexpected ! ");
				throw new VcsException("expected only deferred-drops as deferred");
			}
		} else {
			if (workingRev == null && "archived".equals(type)) {
				return new MksMemberState(null, (MksRevisionNumber.createRevision(memberRev)), workingCpid,
						MksMemberState.Status.REMOTELY_ADDED);
			} else if (workingRev != null && memberRev != null) {
				return new MksMemberState((MksRevisionNumber.createRevision(workingRev)),
						(MksRevisionNumber.createRevision(memberRev)), workingCpid,
						MksMemberState.Status.SYNC);
			} else if (isDropped(type) && workingRev != null) {
				return new MksMemberState((MksRevisionNumber.createRevision(workingRev)),
						(MksRevisionNumber.createRevision(memberRev)), workingCpid,
						MksMemberState.Status.REMOTELY_DROPPED);
			} else {
				LOGGER.warn("unexpected ! ");
				throw new VcsException("unexpected");
			}

		}
	}

}
