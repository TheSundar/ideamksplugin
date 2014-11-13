package org.intellij.vcs.mks.history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.history.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.vcsUtil.VcsUtil;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.NoCredentialsException;
import com.mks.api.response.WorkItem;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.intellij.vcs.mks.model.MksMemberState;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.sicommands.api.ViewChangePackageAPICommand;
import org.intellij.vcs.mks.sicommands.api.ViewMemberHistoryAPICommand;
import org.intellij.vcs.mks.sicommands.api.ViewSandboxCommandAPI;
import org.intellij.vcs.mks.sicommands.cli.GetRevisionInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Allows fetching the history for a particular file. <br/>
 * relies on si viewhistory
 */
public class MksVcsHistoryProvider implements VcsHistoryProvider {
    private final MksVcs vcs;
    private final Logger LOGGER = Logger.getInstance(getClass().getName());
    private final AnAction viewChangePackageAction = new AnAction("View ChangePackage") {
		@Override
		public void update(@NotNull AnActionEvent e) {
			Project project = (Project) e.getData(PlatformDataKeys.PROJECT);
			if (project != null) {
				VcsKey vcsKey = (VcsKey) e.getData(VcsDataKeys.VCS);
				if (vcsKey != null) {
					VcsFileRevision revision = (VcsFileRevision) e.getData(VcsDataKeys.VCS_FILE_REVISION);
					VirtualFile revisionVirtualFile = (VirtualFile) e.getData(VcsDataKeys.VCS_VIRTUAL_FILE);

					MksVcsFileRevision mksRevision = (MksVcsFileRevision) revision;
					String cpid = mksRevision.getCpid();
					e.getPresentation().setEnabled(null != cpid);
				}
			}
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			Project project = (Project) e.getData(PlatformDataKeys.PROJECT);
			if (project != null) {
				VcsKey vcsKey = (VcsKey) e.getData(VcsDataKeys.VCS);
				if (vcsKey != null) {
					VcsFileRevision revision = (VcsFileRevision) e.getData(VcsDataKeys.VCS_FILE_REVISION);
					VirtualFile revisionVirtualFile = (VirtualFile) e.getData(VcsDataKeys.VCS_VIRTUAL_FILE);

					MksVcsFileRevision mksRevision = (MksVcsFileRevision) revision;
					String cpid = mksRevision.getCpid();
					if (null != cpid) {
						new ViewChangePackageAPICommand(new ArrayList<VcsException>(), MksVcs.getInstance(project), cpid).execute();
					}

				}
			}
		}
	};

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
		final FilePath sandboxFolder = sandboxPath.getParentPath();
		assert sandboxFolder != null : "sandbox parent folder can not be null";
		assert filePath.getPath().startsWith(sandboxFolder.getPath()) :
				"" + filePath.getPath() + " should start with " + sandboxFolder.getPath();
		ViewSandboxCommandAPI command = createCommand(sandbox, filePath, sandboxFolder);
		command.execute();
		MksMemberState state = null;
		if (command.foundError()) {
			if (isNotConnectedError(command.errors)) {
				tryReconnect(sandbox);
				command = createCommand(sandbox, filePath, sandboxFolder);
				command.execute();
			} else {
				LOGGER.error("error obtaining current revision for " + filePath);
				throw new VcsException("error obtaining current revision for " + filePath);
			}
		}
		if (!command.foundError()) {

			state = command.getMemberStates().get(filePath.getPath());
			if (state == null) {
				for (String s : command.getMemberStates().keySet()) {
					if (VcsUtil.getFilePath(s).getPath().equals(filePath.getPath())) {
						state = command.getMemberStates().get(s);
						break;
					}
				}
			}
			return (null == state)? null: ((VcsRevisionNumber.NULL == state.workingRevision) ? null : state.workingRevision);
		} else {
			LOGGER.error("error obtaining current revision for " + filePath);
			throw new VcsException("error obtaining current revision for " + filePath);
		}
	}

	private ViewSandboxCommandAPI createCommand(final MksSandboxInfo sandbox, final FilePath filePath, final FilePath sandboxFolder) {
		return new ViewSandboxCommandAPI(new ArrayList<VcsException>(), vcs, sandbox.sandboxPath
//				"--fields=workingrev",
//				"--recurse"
		) {
			@Override
			protected Command createAPICommand() {
				Command apiCommand = super.createAPICommand();
				apiCommand.addOption(new Option("filter", "file:" + MksVcs.getRelativePath(filePath, sandboxFolder)));
				return apiCommand;
			}

			@Override
			protected MksMemberState createState(WorkItem item) throws VcsException {
				return super.createState(item);
			}
		};
	}

	private boolean tryReconnect(MksSandboxInfo sandboxInfo) {

		HashSet<MksSandboxInfo> sandboxes = new HashSet<MksSandboxInfo>();
		sandboxes.add(sandboxInfo);
		MKSAPIHelper helper = MKSAPIHelper.getInstance();
		ArrayList<MksServerInfo> mksServers = helper.getMksServers(null, new ArrayList<VcsException>(), vcs);
		return helper.checkNeededServersAreOnlineAndReconnectIfNeeded(sandboxes, mksServers, vcs.getProject());
	}

	private boolean isNotConnectedError(List<VcsException> errors) {
		for (VcsException error : errors) {
			if (error.getCause() != null && error.getCause() instanceof NoCredentialsException) {
				return true;
			}
		}
		return false;
	}

	private List<VcsFileRevision> getRevisions(FilePath filePath) {
        final ViewMemberHistoryAPICommand command =
                new ViewMemberHistoryAPICommand(new ArrayList<VcsException>(), vcs, filePath.getPath());
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
     * @param runnable
     * @return
     */
    @Override
    public AnAction[] getAdditionalActions(Runnable runnable) {
        return new AnAction[]{viewChangePackageAction};
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
