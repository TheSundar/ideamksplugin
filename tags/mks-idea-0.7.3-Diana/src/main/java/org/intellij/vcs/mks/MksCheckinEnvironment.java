package org.intellij.vcs.mks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Thibaut Fagart
 */
public class MksCheckinEnvironment implements CheckinEnvironment {
	private final Logger logger = Logger.getInstance(MksCheckinEnvironment.class.getName());
	private final MksVcs mksVcs;

	public MksCheckinEnvironment(MksVcs mksVcs) {
		this.mksVcs = mksVcs;
	}

	public List<VcsException> commit(List<Change> changes, String preparedComment) {
		// todo find the appropriate change package
		// commit the changes using the change package
		logger.debug("committing <" + preparedComment + ">");
		debugChanges(changes);
		ArrayList<VirtualFile> modifiedFiles = new ArrayList<VirtualFile>();
		for (Change change : changes) {
			if (FileStatus.MODIFIED.equals(change.getFileStatus())) {
				ContentRevision afterRevision = change.getAfterRevision();
				if (afterRevision != null) {
					FilePath filePath = afterRevision.getFile();
					modifiedFiles.add(VcsUtil.getVirtualFile(filePath.getIOFile()));
				}

			}
		}
		DispatchBySandboxCommand dispatchAction = new DispatchBySandboxCommand(mksVcs,
				modifiedFiles.toArray(new VirtualFile[modifiedFiles.size()]));
		dispatchAction.execute();

		List<VcsException> exceptions = new ArrayList<VcsException>();
		Map<MksSandboxInfo, ArrayList<VirtualFile>> filesBysandbox = dispatchAction.getFilesBySandbox();
		for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : filesBysandbox.entrySet()) {
			TriclopsSiMembers members = MKSHelper.createMembers(entry.getKey());
			for (VirtualFile virtualFile : entry.getValue()) {
				members.addMember(new TriclopsSiMember(virtualFile.getPresentableUrl()));
			}
			try {
				MKSHelper.getMembersStatus(members);
			} catch (TriclopsException e) {
				//noinspection ThrowableInstanceNeverThrown
				exceptions.add(new MksVcsException(getCheckinOperationName() +
						" Error obtaining mks status: " + MksVcs.getMksErrorMessage(), e));
			}
			try {
				MKSHelper.checkinMembers(members, 0);
			} catch (TriclopsException e) {
				if (!MksVcs.isLastCommandCancelled()) {
					//noinspection ThrowableInstanceNeverThrown
					exceptions.add(new MksVcsException(getCheckinOperationName() +
							" Error checking in: " + MksVcs.getMksErrorMessage(), e));
				}
			}
		}

		return exceptions;
	}

	private void debugChanges(List<Change> changes) {
		for (Change change : changes) {

			if (change != null) {
				logger.debug("change " + change.getFileStatus() + " " + (
						(change.getAfterRevision() != null) ? change.getAfterRevision() : "unknown")
				);
			}
		}
	}

	@Nullable
	public RefreshableOnComponent createAdditionalOptionsPanel(CheckinProjectPanel panel) {
		return null;
	}

	public String getCheckinOperationName() {
		return "Check in";
	}

	@Nullable
	public String getDefaultMessageFor(FilePath[] filesToCheckin) {
		return null;
	}

	@Nullable
	@NonNls
	public String getHelpId() {
		return null;
	}

	public String prepareCheckinMessage(String text) {
		return null;
	}

	public List<VcsException> scheduleMissingFileForDeletion(List<FilePath> files) {
		return null;
	}

	public List<VcsException> scheduleUnversionedFilesForAddition(List<VirtualFile> files) {
		return null;
	}

	public boolean showCheckinDialogInAnyCase() {
		return false;
	}

	public boolean keepChangeListAfterCommit(ChangeList changeList) {
		return true;
	}
}
