package org.intellij.vcs.mks.history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.*;
import com.intellij.openapi.vfs.VirtualFile;
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

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
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
    @Override
    public VcsHistorySession createSessionFor(final FilePath filePath) throws VcsException {
        final MksSandboxInfo sandbox = getSandbox(filePath);
        if (sandbox == null) {
            LOGGER.warn("can't find sandbox for " + filePath);
            return null;
        }
        final List<VcsFileRevision> revisions = getRevisions(filePath);
        return new MyVcsAbstractHistorySession(revisions, filePath, sandbox);
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

/*
			@Override
			public void execute() {
				try {
					super.executeCommand();
					BufferedReader reader = new BufferedReader(new StringReader(commandOutput));
					String line ;
					while ((line = reader.readLine()) != null && !"".equals(line)) {
						if (currentRevisionHolder[0] == null) {
							currentRevisionHolder[0] = line;
						} else {
							LOGGER.warn("multiple members retrieved for "+filePath+"!!");
						}
					}

				} catch (IOException e) {
					LOGGER.error("error obtaining current revision for " + filePath, e);
				}

			}
*/
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
                if (GetRevisionInfo.NOT_A_MEMBER.equals(error.getMessage())) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                    Messages.showMessageDialog("Not (or not any more) a member", "title",
                            Messages.getInformationIcon());
                        }
                    };
                    MksVcs.invokeLaterOnEventDispatchThread(runnable);
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
		return getSandbox(filePath.getVirtualFile());
    }

	private MksSandboxInfo getSandbox(VirtualFile virtualFile) {
		return vcs.getSandboxCache().getSubSandbox(virtualFile);
	}

	/**
     * @param vcsHistorySession
     * @return
     * @deprecated
     */
    public ColumnInfo[] getRevisionColumns(VcsHistorySession vcsHistorySession) {
        return getRevisionColumns();
    }

    /**
     * todo : add possibility to view change package related to a revision
     *
     * @param runnable
     * @return
     */
    @Override
    public AnAction[] getAdditionalActions(Runnable runnable) {
        return new AnAction[0];
    }

    @Nullable
    @NonNls
    @Override
    public String getHelpId() {
        return null;
    }

    public ColumnInfo<VcsFileRevision, String>[] getRevisionColumns() {
        final ColumnInfo<VcsFileRevision, String> myColumnInfo =
                new ColumnInfo<VcsFileRevision, String>("change package") {
                    @Override
                    public String valueOf(VcsFileRevision vcsFileRevision) {

                        if (vcsFileRevision instanceof MksVcsFileRevision) {
                            return ((MksVcsFileRevision) vcsFileRevision).getCpid();
                        } else {
                            return "unknown";
                        }
                    }
                };
        //noinspection unchecked
        final ColumnInfo<VcsFileRevision, String>[] array =
                (ColumnInfo<VcsFileRevision, String>[]) Array.newInstance(myColumnInfo.getClass(), 1);
        array[0] = myColumnInfo;
        return array;
    }//return null if your revisions cannot be tree


    @Override
    public boolean supportsHistoryForDirectories() {
        return false;
    }

    @Override
    public boolean isDateOmittable() {
        return false;
    }

    @Override
    public VcsDependentHistoryComponents getUICustomization(
            VcsHistorySession session, JComponent forShortcutRegistration) {
        return new VcsDependentHistoryComponents(getRevisionColumns(session), null, null);
    }


    @Override
    public void reportAppendableHistory(FilePath filePath,
                                        VcsAppendableHistorySessionPartner partner) throws VcsException {
        final MksSandboxInfo sandbox = getSandbox(filePath);
        if (sandbox == null) {
            LOGGER.warn("can't find sandbox for " + filePath);
            return;
        }

        final VcsAbstractHistorySession emptySession = new MyVcsAbstractHistorySession(Collections.<VcsFileRevision>emptyList(), filePath, sandbox);
        partner.reportCreatedEmptySession(emptySession);
        final List<VcsFileRevision> revisions = getRevisions(filePath);
        for (VcsFileRevision revision : revisions) {
            partner.acceptRevision(revision);
        }

    }

	@Override
	public boolean canShowHistoryFor(@NotNull VirtualFile virtualFile) {
		return !virtualFile.isDirectory()
				&& getSandbox(virtualFile) != null;

	}

	@Nullable
	@Override
	public DiffFromHistoryHandler getHistoryDiffHandler() {
		return null;
	}

	private class MyVcsAbstractHistorySession extends VcsAbstractHistorySession {
        final boolean isDirectory;
        @NotNull
        private final FilePath filePath;
        @NotNull
        private final MksSandboxInfo sandbox;

        public MyVcsAbstractHistorySession(List<VcsFileRevision> revisions, @NotNull FilePath filePath, @NotNull MksSandboxInfo sandbox) {
            super(revisions);
            this.filePath = filePath;
            this.sandbox = sandbox;
            isDirectory = filePath.isDirectory();
            shouldBeRefreshed();
        }

        @Nullable
        @Override
        public HistoryAsTreeProvider getHistoryAsTreeProvider() {
            return new MksMemberHistoryAsTreeProvider();
        }

        @Override
        @Nullable
        public VcsRevisionNumber calcCurrentRevisionNumber() {
            if (filePath == null || sandbox == null) {
                return null;
            }
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

        @Override
        public VcsHistorySession copy() {
            return new MyVcsAbstractHistorySession(this.getRevisionList(), this.filePath, sandbox);
        }
    }
}
