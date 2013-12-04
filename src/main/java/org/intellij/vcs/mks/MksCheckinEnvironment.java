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
import com.intellij.util.NullableFunction;
import com.intellij.util.PairConsumer;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.actions.api.AddMemberAPICommand;
import org.intellij.vcs.mks.actions.api.CheckinAPICommand;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Thibaut Fagart
 */
public class MksCheckinEnvironment implements CheckinEnvironment {
	private final Logger logger = Logger.getInstance(MksCheckinEnvironment.class.getName());
	private final MksVcs mksVcs;

	public MksCheckinEnvironment(MksVcs mksVcs) {
		this.mksVcs = mksVcs;
	}

	@Override
	public List<VcsException> commit(List<Change> changes, String preparedComment) {
		// todo find the appropriate change package
		// commit the changes using the change package
		logger.debug("committing <" + preparedComment + ">");
		debugChanges(changes);
		ArrayList<VirtualFile> modifiedFiles = new ArrayList<VirtualFile>();
		ArrayList<VirtualFile> addedFiles = new ArrayList<VirtualFile>();
        List<VcsException> exceptions = new ArrayList<VcsException>();

		for (Change change : changes) {
			if (FileStatus.MODIFIED.equals(change.getFileStatus())) {
				ContentRevision afterRevision = change.getAfterRevision();
				if (afterRevision != null) {
					FilePath filePath = afterRevision.getFile();
					modifiedFiles.add(VcsUtil.getVirtualFile(filePath.getIOFile()));
				}

			} else if (FileStatus.ADDED.equals(change.getFileStatus())) {
                addedFiles.add(change.getAfterRevision().getFile().getVirtualFile());
            }else {
                    exceptions.add(new VcsException("only MODIFIED/ADDED (!= "+change.getFileStatus()+"files are currently supported" ));
            }
        }
        CheckinAPICommand checkinAPICommand = new CheckinAPICommand();
        try {
            checkinAPICommand.executeCommand(mksVcs, exceptions, modifiedFiles.toArray(new VirtualFile[modifiedFiles.size()]));
        } catch (VcsException e) {
            //noinspection ThrowableInstanceNeverThrown
            exceptions.add(e);
        }
        AddMemberAPICommand addMemberAPICommand = new AddMemberAPICommand();
        try {
            addMemberAPICommand.executeCommand(mksVcs, exceptions, addedFiles.toArray(new VirtualFile[addedFiles.size()]));
        } catch (VcsException e) {
            //noinspection ThrowableInstanceNeverThrown
            exceptions.add(e);
        }
/*        DispatchBySandboxCommand dispatchAction = new DispatchBySandboxCommand(mksVcs,
				modifiedFiles.toArray(new VirtualFile[modifiedFiles.size()]));
		dispatchAction.execute();

		Map<MksSandboxInfo, ArrayList<VirtualFile>> filesBysandbox = dispatchAction.getFilesBySandbox();
		for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : filesBysandbox.entrySet()) {

            if (entry.getKey() instanceof MksNativeSandboxInfo) {
                TriclopsSiMembers members = MKSHelper.createMembers(((MksNativeSandboxInfo) entry.getKey()));
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
            }               else {
                throw new UnsupportedOperationException("not supported for non native");
            }
        }*/

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
	@Override
	public RefreshableOnComponent createAdditionalOptionsPanel(CheckinProjectPanel checkinProjectPanel, PairConsumer<Object, Object> objectObjectPairConsumer) {
		return null;
	}

	@Override
	public String getCheckinOperationName() {
		return "Check in";
	}

	@Nullable
	@Override
	public String getDefaultMessageFor(FilePath[] filesToCheckin) {
		return null;
	}

	@Nullable
	@NonNls
	@Override
	public String getHelpId() {
		return null;
	}

	@Override
	public List<VcsException> scheduleMissingFileForDeletion(List<FilePath> files) {
		return null;
	}

	@Override
	public List<VcsException> scheduleUnversionedFilesForAddition(List<VirtualFile> files) {
		return null;
	}

	/**
	 * todo
	 * @param changeList
	 * @return
	 */
	@Override
	public boolean keepChangeListAfterCommit(ChangeList changeList) {
		return false;
	}

	/**
	 * todo
	 */
	@Override
	@Nullable
	public List<VcsException> commit(List<Change> changes, String preparedComment, @NotNull NullableFunction<Object, Object> parametersHolder, Set<String> feedback) {
		return commit(changes, preparedComment);
	}

	@Override
	public boolean isRefreshAfterCommitNeeded() {
		return false;
	}
}
