package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksMemberState;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This commands fetches any file from the given sandbox that is
 * - locked by the user (in thsi sandbox)
 * - or modified without checkout
 * - or have a deferred operation associated with it
 * <p/>
 * It may miss files that have been BOTH modified without checkout in the current sandbox AND locked in another one
 *
 * @author Thibaut Fagart
 */
public class ViewSandboxLocalChangesOrLockedCommand extends AbstractViewSandboxCommand {
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
	public ViewSandboxLocalChangesOrLockedCommand(final List<VcsException> errors, final EncodingProvider encodingProvider,
												  final String username, final String sandboxPath) {
		super(errors, encodingProvider, sandboxPath,/* "--filter=changed",*/"--filter=changed:working,deferred,locked:" + username);
		this.mksUsername = username;
	}

	@Override
	protected MksMemberState createState(final String workingRev, final String memberRev, final String workingCpid,
										 final String locker, final String lockedSandbox, final String type, final String deferred) throws VcsException {
		// we confuse missing files and locally modified without checkout here
		boolean isLocked = locker != null;
		if (isDeferred(deferred)) {
			return createDeferredState(workingRev, memberRev, workingCpid, type);
		}
		if (isLocked) {
			MksMemberState.Status status;
			if (isLockedByMe(locker)) {
				if (isMySandbox(lockedSandbox)) {
					status = MksMemberState.Status.CHECKED_OUT;
				} else {
					// we're seeing a file modified by the user in another sandbox, but we don't know if it has been locally modified or not
					status = MksMemberState.Status.UNKNOWN;
				}
			} else {
				status = MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT;
			}
			return new MksMemberState((createRevision(workingRev)), (createRevision(memberRev)), workingCpid,
					status);

		} else {
			return new MksMemberState((createRevision(workingRev)), (createRevision(memberRev)), workingCpid,
					MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);

		}

	}

	private boolean isMySandbox(final String lockedSandbox) {
		if (lockedSandbox == null) {
			return false;
		}
		File sandboxPjFile = new File(this.sandboxPath);
		File lockedSandboxPjFile = new File(lockedSandbox);
		try {
			return // same sandbox or a subsandbox
					lockedSandboxPjFile.getCanonicalPath().equals(sandboxPjFile.getCanonicalPath())
							|| FileUtil.isAncestor(sandboxPjFile.getParentFile(), lockedSandboxPjFile, false);
		} catch (IOException e) {
			LOGGER.warn("exception comparing locked sandbox and current sandbox, assuming different", e);
			return false;
		}
	}

	private boolean isLockedByMe(final String locker) {
		return mksUsername.equals(locker);
	}

}
