package org.intellij.vcs.mks.history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.*;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.intellij.vcs.mks.model.MksMemberState;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.sicommands.AbstractViewSandboxCommand;
import org.intellij.vcs.mks.sicommands.GetRevisionInfo;
import org.intellij.vcs.mks.sicommands.ViewMemberHistoryCommand;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows fetching the history for a particular file. <br/>
 * relies on si viewhistory
 */
public class MksVcsHistoryProvider implements VcsHistoryProvider {
	private final MksVcs vcs;
	private final Logger LOGGER = Logger.getInstance(getClass().getName());

	public MksVcsHistoryProvider(MksVcs vcs) {
		this.vcs = vcs;
	}

	@Nullable
	public VcsHistorySession createSessionFor(final FilePath filePath) throws VcsException {
		final boolean isDirectory = filePath.isDirectory();
		final MksSandboxInfo sandbox = getSandbox(filePath);
		if (sandbox == null) {
			LOGGER.warn("can't find sandbox for " + filePath);
			return null;
		}
		final List<VcsFileRevision> myRevisions = getRevisions(filePath);
		return new VcsHistorySession(myRevisions) {
			@Override
			@Nullable
			public VcsRevisionNumber calcCurrentRevisionNumber() {
				try {
					return getCurrentRevision(sandbox, filePath);
				} catch (VcsException e) {
					LOGGER.warn(e.getMessage(), e);
					return null;
				}
			}

			@Override
			public boolean isContentAvailable(final VcsFileRevision revision) {
				return !isDirectory;
			}
		};
	}

	/**
	 * @param sandbox  the sandbox this file belongs to
	 * @param filePath the file whose revision we need
	 * @return the current working revision the sandbox uses
	 * @throws VcsException if fetching the revision fails
	 */
	@Nullable
	private VcsRevisionNumber getCurrentRevision(@NotNull MksSandboxInfo sandbox,
												 @NotNull final FilePath filePath) throws VcsException {
		FilePath sandboxPath = VcsUtil.getFilePath(sandbox.sandboxPath);
		FilePath sandboxFolder = sandboxPath.getParentPath();
		assert sandboxFolder != null : "sandbox parent folder can not be null";
		assert filePath.getPath().startsWith(sandboxFolder.getPath()) :
				"" + filePath.getPath() + " should start with " + sandboxFolder.getPath();
		final AbstractViewSandboxCommand command =
				new AbstractViewSandboxCommand(new ArrayList<VcsException>(), vcs, sandbox.sandboxPath
						, "--filter=file:" + MKSHelper.getRelativePath(filePath, sandboxFolder)
//				"--fields=workingrev",
//				"--recurse"
				) {
					@Override
					protected MksMemberState createState(String workingRev, String memberRev, String workingCpid,
														 String locker, String lockedSandbox, String type,
														 String deferred) throws VcsException {
						return new MksMemberState((MksRevisionNumber.createRevision(workingRev)),
								(MksRevisionNumber.createRevision(memberRev)), workingCpid,
								MksMemberState.Status.UNKNOWN);
					}
				};
		command.execute();
		MksMemberState state = command.getMemberStates().get(filePath.getPath());
		if (state == null) {
			for (String s : command.getMemberStates().keySet()) {
				if (VcsUtil.getFilePath(s).getPath().equals(filePath.getPath())) {
					state = command.getMemberStates().get(s);
					break;
				}
			}
		}
		if (state == null) {
			LOGGER.error("error obtaining current revision for " + filePath);
			throw new VcsException("error obtaining current revision for " + filePath);
		}
		return (VcsRevisionNumber.NULL == state.workingRevision) ? null : state.workingRevision;

	}

	private List<VcsFileRevision> getRevisions(FilePath filePath) {
		final ViewMemberHistoryCommand command =
				new ViewMemberHistoryCommand(new ArrayList<VcsException>(), vcs, filePath.getPath());
		command.execute();
		if (command.foundError()) {
			for (VcsException error : command.errors) {
				if (error.getMessage().equals(GetRevisionInfo.NOT_A_MEMBER)) {
					Messages.showMessageDialog("Not (or not any more) a member", "title",
							Messages.getInformationIcon());
				} else {
					LOGGER.warn(error);
				}
			}
		}
		final List<MksMemberRevisionInfo> revisions = command.getRevisionsInfo();
		final ArrayList<VcsFileRevision> vcsRevisions = new ArrayList<VcsFileRevision>(revisions.size());
		for (MksMemberRevisionInfo revision : revisions) {
			vcsRevisions.add(new MksVcsFileRevision(vcs, filePath, revision));
		}
		return vcsRevisions;
	}

	private MksSandboxInfo getSandbox(FilePath filePath) {
		return vcs.getSandboxCache().getSubSandbox(filePath.getVirtualFile());
	}

	public ColumnInfo[] getRevisionColumns(VcsHistorySession vcsHistorySession) {
		return getRevisionColumns();
	}
	private static final class CpidColumnInfo extends ColumnInfo<VcsFileRevision, String> {
		private CpidColumnInfo() {
			super("Cpid");
		}

		public String valueOf(VcsFileRevision vcsFileRevision) {
			return (vcsFileRevision instanceof MksVcsFileRevision) ?
					((MksVcsFileRevision)vcsFileRevision).getCpid() : "";
		}
	}

	public AnAction[] getAdditionalActions(FileHistoryPanel panel) {
		return new AnAction[0];
	}

	@Nullable
	@NonNls
	public String getHelpId() {
		return null;
	}
	/**
	 * @deprecated from IDEA7.x
	 */
	private ColumnInfo<VcsFileRevision, String>[] getRevisionColumns() {
		//noinspection unchecked
		return new ColumnInfo[]{new CpidColumnInfo()};
	}//return null if your revisions cannot be tree

	@Nullable
	public HistoryAsTreeProvider getTreeHistoryProvider() {
		return new MksMemberHistoryAsTreeProvider();
	}

	public boolean supportsHistoryForDirectories() {
		return false;
	}

	public boolean isDateOmittable() {
		return false;
	}
}
