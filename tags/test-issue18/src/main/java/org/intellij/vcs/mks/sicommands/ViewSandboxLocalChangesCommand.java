package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksMemberState;

import java.io.File;
import java.util.List;

/**
 * Command backed by si viewsandbox. Allows fetching sandbox deltas, complete
 * state of the sandbox is given by {@link org.intellij.vcs.mks.sicommands.ViewSandboxWithoutChangesCommand}
 * Locally deleted files aren't distinguished from  modified without checkout
 * ones
 *
 * @author Thibaut Fagart
 */
public class ViewSandboxLocalChangesCommand extends AbstractViewSandboxCommand {
	protected final String mksUsername;

	/**
	 * username is available in si viewservers
	 *
	 * @param errors		   collects all errors encountered
	 * @param encodingProvider provides encoding configuration for the command
	 * @param sandboxPath	  filepath of the sandbox project file (usually
	 *                         project.pj)
	 * @param username		 username of the current user : allows detecting
	 *                         which locks are checkouts of the IDEA user
	 */
	public ViewSandboxLocalChangesCommand(final List<VcsException> errors, final EncodingProvider encodingProvider,
										  final String username, final String sandboxPath) {
		super(errors, encodingProvider, sandboxPath,/* "--filter=changed",*/"--filter=changed:working,deferred,locked:" + username);
		this.mksUsername = username;
	}

	@Override
	protected MksMemberState createState(final String workingRev, final String memberRev, final String workingCpid,
										 final String locker, final String lockedSandbox, final String type, final String deferred) throws VcsException {
		// we confuse missing files and locally modified without checkout here
		boolean isLocked = locker != null;
		if (DEFERRED.equals(deferred)) {
			MksMemberState.Status status;

			if (DEFERRED_ADD.equals(type)) {
				status = MksMemberState.Status.ADDED;
			} else if (DEFERRED_DROP.equals(type)) {
				status = MksMemberState.Status.DROPPED;
			} else {
				status = MksMemberState.Status.UNKNOWN;
			}

			return new MksMemberState(createRevision(workingRev), createRevision(memberRev), workingCpid,
					status);
		}
		if (isLocked) {
			MksMemberState.Status status;
			if (isLockedByMe(locker)) {
				if (isMySandbox(lockedSandbox)) {
					status = MksMemberState.Status.CHECKED_OUT;
				} else {
					status = MksMemberState.Status.NOT_CHANGED;
				}
			} else {
				status = MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT;
			}
			return new MksMemberState(createRevision(workingRev), createRevision(memberRev), workingCpid,
					status);

		} else {
			return new MksMemberState(createRevision(workingRev), createRevision(memberRev), workingCpid,
					MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);

		}

	}

	private boolean isMySandbox(final String lockedSandbox) {
		if (lockedSandbox == null) {
			return false;
		}
		File sandboxPjFile = new File(this.sandboxPath);
		String sandboxFolder = sandboxPjFile.getParent();
		File lockedSandboxPjFile = new File(lockedSandbox);
		return lockedSandboxPjFile.getAbsolutePath().startsWith(sandboxFolder);
	}

	private boolean isLockedByMe(final String locker) {
		return mksUsername.equals(locker);
	}

}