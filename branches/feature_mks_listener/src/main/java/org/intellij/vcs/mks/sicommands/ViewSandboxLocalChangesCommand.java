package org.intellij.vcs.mks.sicommands;

import java.io.File;
import java.util.List;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.model.MksMemberState;
import com.intellij.openapi.vcs.VcsException;

/**
 * Command backed by si viewsandbox. Allows fetching sandbox deltas, complete
 * state of the sandbox is given by {@link org.intellij.vcs.mks.sicommands.ViewSandboxWithoutChangesCommand}
 * Locally deleted files aren't distinguished from  modified without checkout ones
 *
 * @author Thibaut Fagart
 */
public class ViewSandboxLocalChangesCommand extends AbstractViewSandboxCommand {

	/**
	 * username is available in si viewservers
	 *
	 * @param errors           collects all errors encountered
	 * @param encodingProvider provides encoding configuration for the command
	 * @param sandboxPath      filepath of the sandbox project file (usually
	 *                         project.pj)
	 * @param username         username of the current user : allows detecting
	 *                         which locks are checkouts of the IDEA user
	 */
	public ViewSandboxLocalChangesCommand(final List<VcsException> errors, final EncodingProvider encodingProvider,
	                                 final String username, final String sandboxPath) {
		super(errors, encodingProvider, username, sandboxPath,/* "--filter=changed",*/"--filter=changed:working,locked:"+username);
	}

	@Override
	protected MksMemberState createState(final String workingRev, final String memberRev, final String workingCpid,
	                                  final String locker, final String lockedSandbox, final String type) throws VcsException {
		// we confuse missing files and locally modified without checkout here
		boolean isLocked = locker != null;
		if (isLocked) {
			MksMemberState.Status status;
			if (isLockedByMe(locker)) {
				if (isMySandbox(lockedSandbox)) {
					status = MksMemberState.Status.CHECKED_OUT ;
				} else {
					status = MksMemberState.Status.NOT_CHANGED;
				}
			} else {
				status = MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT;
			}
			return new MksMemberState(new MksRevisionNumber(workingRev), new MksRevisionNumber(memberRev), workingCpid,
				status);

		} else {
			return new MksMemberState(new MksRevisionNumber(workingRev), createMemberRev(memberRev, type), workingCpid,
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