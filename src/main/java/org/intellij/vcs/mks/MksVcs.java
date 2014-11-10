package org.intellij.vcs.mks;

import com.intellij.ProjectTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.rollback.RollbackEnvironment;
import com.intellij.openapi.vcs.update.UpdateEnvironment;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.AbstractTableCellEditor;
import org.intellij.vcs.mks.actions.api.CheckoutAPICommand;
import org.intellij.vcs.mks.history.MksFileAnnotation;
import org.intellij.vcs.mks.history.MksVcsFileRevision;
import org.intellij.vcs.mks.history.MksVcsHistoryProvider;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.intellij.vcs.mks.realtime.*;
import org.intellij.vcs.mks.sicommands.api.ListServersAPI;
import org.intellij.vcs.mks.sicommands.api.ViewMemberHistoryAPICommand;
import org.intellij.vcs.mks.sicommands.cli.*;
import org.intellij.vcs.mks.update.MksUpdateEnvironment;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MksVcs extends AbstractVcs implements MksCLIConfiguration {
    static final Logger LOGGER = Logger.getInstance(MksVcs.class.getName());
    static final boolean DEBUG = false;

    private JTextPane mksTextArea;
    private final SandboxCache sandboxCache;
    private MessageBusConnection myMessageBusConnection;
    private MksVcs.TasksModel tasksModel;

    private final MksCheckinEnvironment mksCheckinEnvironment = new MksCheckinEnvironment(this);
    //private final MksRollbackEnvironment rollbackEnvironment= new MksRollbackEnvironment(this);
    private final MksChangeListAdapter changeListAdapter = new MksChangeListAdapter(this);
    private final EditFileProvider editFileProvider = new _EditFileProvider(this);
    private final MksDiffProvider diffProvider = new MksDiffProvider(this);
    private final VcsHistoryProvider vcsHistoryProvider = new MksVcsHistoryProvider(this);
    private final MksUpdateEnvironment updateEnvironment = new MksUpdateEnvironment(this);
    @NonNls
    public static final String VCS_NAME = "MKS";
    public static final VcsKey OUR_KEY = createKey(VCS_NAME);
    private static final String MKS_TOOLWINDOW = "MKS";
    @NonNls
    public static final String PROJECT_PJ_FILE = "project.pj";
    @NonNls
    private static final String ICONS_MKS_GIF = "/icons/mks.gif";
    private Boolean isMks2007 = null;
    private MksRollbackEnvironment rollbackEnvironment = new MksRollbackEnvironment(this);

    public MksVcs(final Project project) {
        super(project, VCS_NAME);
        sandboxCache = project.getComponent(SandboxCache.class);


//        sandboxCache = new NativeSandboxCacheImpl(project);
    }

	public static void invokeOnEventDispatchThreadAndWait(Runnable runnable) throws VcsException {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (InterruptedException e1) {
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException e1) {
				throw new VcsException(e1.getTargetException());
			}
		} else {
			runnable.run();
		}
	}
	public static void invokeLaterOnEventDispatchThread(Runnable runnable) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(runnable);
		} else {
			runnable.run();
		}
	}

    public static String getRelativePath(@NotNull FilePath filePath, @NotNull FilePath parentPath) {
        return VfsUtil.getRelativePath(filePath.getVirtualFile(), parentPath.getVirtualFile(), '/');
    }

    public static String getRelativePath(@NotNull VirtualFile virtualFile, @NotNull VirtualFile parentVirtualFile) {
		return VfsUtil.getRelativePath(virtualFile, parentVirtualFile, '/');
	}


    @Override
    public Configurable getConfigurable() {
        return new MksConfigurableForm(ApplicationManager.getApplication().getComponent(MksConfiguration.class));
    }

    @Override
    public String getDisplayName() {
        return VCS_NAME;
    }

    public MksChangeListAdapter getChangeListAdapter() {
        return changeListAdapter;
    }

    public void showErrors(final java.util.List<VcsException> list, final String action) {
        if (!list.isEmpty()) {
            final StringBuffer buffer = new StringBuffer(mksTextArea.getText());
            buffer.append("\n");
            buffer.append(action).append(" Error: ");
            VcsException e;
            for (Iterator<VcsException> iterator = list.iterator(); iterator.hasNext(); buffer.append(e.getMessage())) {
                //noinspection ThrowableResultOfMethodCallIgnored
                e = iterator.next();
                buffer.append("\n");
            }
            mksTextArea.setText(buffer.toString());
        }
    }

    private void initToolWindow() {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
        final JToolBar toolbar = new JToolBar();
        final Action viewSandboxesAction = new AbstractAction("view sandboxes") {
            public void actionPerformed(final ActionEvent event) {
                final StringWriter stringWriter = new StringWriter();
                final PrintWriter pw = new PrintWriter(stringWriter);
                pw.println(mksTextArea.getText());
                sandboxCache.dumpStateOn(pw);
                mksTextArea.setText(stringWriter.toString());
            }
        };
        toolbar.add(viewSandboxesAction);
        final JPanel mksPanel = new JPanel(new BorderLayout());
        mksPanel.add(toolbar, BorderLayout.NORTH);
        final JTabbedPane tabbedPane = new JTabbedPane();

        this.mksTextArea = createMksLogTextPane();
        final JPanel panelDebug = new JPanel(new BorderLayout());
        panelDebug.add(new JScrollPane(mksTextArea), BorderLayout.CENTER);
        tabbedPane.add(panelDebug, "Log", 0);


        tabbedPane.add(createCommandStatisticsPanel(), "Command statistics");
        tabbedPane.add(createTasksPanel(), "Daemon processes");
        mksPanel.add(tabbedPane, BorderLayout.CENTER);
        invokeLaterOnEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                registerToolWindow(toolWindowManager, mksPanel);
            }
        });

        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem item = new JMenuItem("Clear");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                mksTextArea.setText("");
            }
        });
        menu.add(item);
        mksTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                maybeShowPopup(e, menu);
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                maybeShowPopup(e, menu);
            }
        });
    }

    class TasksModel extends AbstractTableModel {
        private final String[] COLUMNS = {"Name", "Running", "Restart"};
        final int NAME = 0;
        final int STATE = 1;
        final int RESTART = 2;


        public int getColumnCount() {
            return COLUMNS.length;
        }

        public int getRowCount() {
            return myProject.getComponent(LongRunningTaskRepository.class).size();
        }

        public Object getValueAt(final int row, final int column) {
            final LongRunningTask task = myProject.getComponent(LongRunningTaskRepository.class).get(row);
            if (task == null) {
                return null;
            }
            switch (column) {
                case NAME:
                    return task.getDescription();
                case STATE:
                    return task.isAlive();
                case RESTART:
                    return false;
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(final int row, final int column) {
            return RESTART == column;
        }

        @Override
        public Class<?> getColumnClass(final int column) {
            switch (column) {
                case NAME:
                    return String.class;
                case STATE:
                    return Boolean.class;
                case RESTART:
                    return Boolean.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public String getColumnName(final int column) {
            return COLUMNS[column];
        }

        public void refresh() {
            fireTableDataChanged();
        }

    }

    private Component createTasksPanel() {
        final JTable tasksTable = new JTable();

        final JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(new JButton(new AbstractAction("Refresh") {
            public void actionPerformed(final ActionEvent event) {
                tasksModel.refresh();
            }
        }));
        tasksModel = new TasksModel();
        jPanel.add(buttonsPanel, BorderLayout.NORTH);
        jPanel.add(new JScrollPane(tasksTable), BorderLayout.CENTER);
        tasksTable.setModel(tasksModel);
        tasksTable.getColumnModel().
                getColumn(tasksModel.RESTART).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(final JTable jTable, final Object o, final boolean b,
                                                           final boolean b1, final int i, final int i1) {
                return new JButton(new AbstractAction("restart") {
                    public void actionPerformed(final ActionEvent e) {
                        final SandboxListSynchronizer synchronizer =
                                ApplicationManager.getApplication().getComponent(SandboxListSynchronizer.class);
                        synchronizer.restart();
                    }
                });
            }
        });
        tasksTable.getColumnModel().getColumn(tasksModel.RESTART).setCellEditor(new AbstractTableCellEditor() {
            public Object getCellEditorValue() {
                return true;
            }

            public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row,
                                                         final int column) {
                final JButton button = new JButton("restart");
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        final LongRunningTask task = myProject.getComponent(LongRunningTaskRepository.class).get(row);
                        LOGGER.debug("restarting task " + task);
                        task.restart();
                    }
                });
                return button;
            }
        });
        return jPanel;

    }

    private final CommandExecutionAdapter commandExecutionAdapter = new CommandExecutionAdapter();

    public CommandExecutionListener getCommandExecutionListener() {
        return commandExecutionAdapter;
    }

    public boolean isMks2007() {
        if (null == this.isMks2007) {
            final SiAboutCommand command =
                    new SiAboutCommand(new ArrayList<VcsException>(), this);
            command.execute();
            if (!command.foundError()) {
                this.isMks2007 = command.isMks2007();
            }
        }
        return this.isMks2007;
    }

    private Component createCommandStatisticsPanel() {
        final JTable commandsTable = new JTable();

        final JPanel jPanel = new JPanel();
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(new JButton(new AbstractAction("Clear statistics") {
            public void actionPerformed(final ActionEvent event) {
                commandExecutionAdapter.clear();
            }
        }));
        jPanel.setLayout(new BorderLayout());
        jPanel.add(buttonsPanel, BorderLayout.NORTH);
        jPanel.add(new JScrollPane(commandsTable), BorderLayout.CENTER);
        commandsTable.setModel(commandExecutionAdapter);
        return jPanel;

    }

    private JTextPane createMksLogTextPane() {
        final JTextPane mksTextArea = new JTextPane();
        mksTextArea.setEditable(false);
        final javax.swing.text.Style def = StyleContext.getDefaultStyleContext().getStyle("default");
        final javax.swing.text.Style regular = mksTextArea.addStyle("REGULAR", def);
        StyleConstants.setFontFamily(def, "SansSerif");
        javax.swing.text.Style s = mksTextArea.addStyle(StyleConstants.Italic.toString(), regular);
        StyleConstants.setItalic(s, true);
        s = mksTextArea.addStyle(StyleConstants.Bold.toString(), regular);
        StyleConstants.setBold(s, true);
        return mksTextArea;
    }

    private ToolWindow registerToolWindow(final ToolWindowManager toolWindowManager, final JPanel mksPanel) {
        final ToolWindow toolWindow = toolWindowManager.registerToolWindow(MKS_TOOLWINDOW, true, ToolWindowAnchor.BOTTOM);
        final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        final Content content = contentFactory.createContent(mksPanel, "", false); // first arg is a JPanel
        content.setCloseable(false);
        toolWindow.getContentManager().addContent(content);

        toolWindow.setIcon(IconLoader.getIcon(ICONS_MKS_GIF, getClass()));
        return toolWindow;
    }

    private void maybeShowPopup(final MouseEvent e, final JPopupMenu menu) {
        if (e.isPopupTrigger()) {
            menu.show(mksTextArea, e.getX(), e.getY());
        }
    }

    private void unregisterToolWindow() {
        ToolWindowManager.getInstance(myProject).unregisterToolWindow("MKS");
    }

    public static MksVcs getInstance(final Project project) {
        return (MksVcs) ProjectLevelVcsManager.getInstance(project).findVcsByName(VCS_NAME);
    }


    public void debug(final String s) {
        ProjectLevelVcsManager.getInstance(myProject).addMessageToConsoleWindow(s, null);
        debug(s, null);
    }

    /**
     * checks if the file is in a directory controlled by mks and is not a mks project file
     *
     * @param filePath the file designation
     * @return true if the file is in a directory controlled by mks
     */
    @Override
    public synchronized boolean fileIsUnderVcs(final FilePath filePath) {
//		System.out.println("super.fileIsUnderVcs " + filePath + " = " + super.fileIsUnderVcs(filePath));
        return super.fileIsUnderVcs(filePath)
                && !getSandboxCache().isSandboxProject(filePath.getVirtualFile());
    }

    private void debug(final String s, final Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println(s);
        if (e != null) {
            LOGGER.debug(s, e);
            e.printStackTrace(pw);
        } else {
            LOGGER.debug(s);
        }
        if (mksTextArea != null) {
            pw.flush();
            try {
                mksTextArea.getDocument().insertString(mksTextArea.getDocument().getLength(), sw.toString(), null);
            } catch (BadLocationException e1) {
                LOGGER.warn(e1);
            }
        }
    }

    @Override
    public DiffProvider getDiffProvider() {
        return diffProvider;
    }

    public Project getProject() {
        return myProject;
    }

    @Override
    @NotNull
    public ChangeProvider getChangeProvider() {
        return myProject.getComponent(MKSChangeProvider.class);
    }

    @Override
    @NotNull
    public String getMksSiEncoding(final String command) {
        return ApplicationManager.getApplication().getComponent(MksConfiguration.class).getMksSiEncoding(command);
    }

    @Override
    @NotNull
    public String getDatePattern() {
        return ApplicationManager.getApplication().getComponent(MksConfiguration.class).getDatePattern();
    }

    private class _EditFileProvider implements EditFileProvider {
        private final MksVcs mksVcs;

        public _EditFileProvider(final MksVcs mksVcs) {
            this.mksVcs = mksVcs;
        }

		@Override
		public void editFiles(final VirtualFile[] virtualFiles) throws VcsException {
			final DispatchBySandboxCommand dispatchCommand = new DispatchBySandboxCommand(mksVcs, virtualFiles);
			dispatchCommand.execute();
			if (!dispatchCommand.getNotInSandboxFiles().isEmpty()) {
				Runnable runnable = new Runnable() {
					public void run() {
						Messages.showErrorDialog(MksBundle.message("unable.to.find.the.sandboxes.for.the.files.title"),
								MksBundle.message("could.not.start.checkout"));
					}
				};
				MksVcs.invokeLaterOnEventDispatchThread(runnable);

				return;
			}
			for (final Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : dispatchCommand.filesBySandbox.entrySet()) {
				final MksSandboxInfo sandbox = entry.getKey();
				final ArrayList<VirtualFile> files = entry.getValue();
				final List<VcsException> errors = new ArrayList<VcsException>();

                final CheckoutAPICommand command = new CheckoutAPICommand();
                command.executeCommand(MksVcs.this, errors, files.toArray(new VirtualFile[files.size()]));
                    if (!errors.isEmpty()) {
                        //noinspection ThrowableResultOfMethodCallIgnored
                        Runnable runnable = new Runnable() {
                            public void run() {
                                Messages.showErrorDialog(errors.get(0).getLocalizedMessage(),
                                        MksBundle.message("could.not.start.checkout"));
                            }
                        };
                        MksVcs.invokeLaterOnEventDispatchThread(runnable);

                        return;
                    }
            }
		}

        @Override
        public String getRequestText() {
            return MksBundle.message("edit.file.provider.request.text");
        }
    }

    @Override
    @Nullable
    public EditFileProvider getEditFileProvider() {
        return editFileProvider;
    }

    public Map<MksSandboxInfo, ArrayList<VirtualFile>> dispatchBySandbox(final VirtualFile[] files, final boolean topSandboxOnly) {
        final DispatchBySandboxCommand dispatchCommand =
                new DispatchBySandboxCommand(this, files, topSandboxOnly);
        dispatchCommand.execute();
        return dispatchCommand.filesBySandbox;
    }

    public Map<MksSandboxInfo, ArrayList<VirtualFile>> dispatchBySandbox(final VirtualFile[] files) {
        return dispatchBySandbox(files, true);
    }

    /**
     * @return the list of available si commands, used for encoding settings
     */
    public static String[] getCommands() {
        return new String[]{
                AbstractViewSandboxCommand.COMMAND,
                GetContentRevision.COMMAND,
                GetRevisionInfo.COMMAND,
                ListChangePackages.COMMAND,
                ListSandboxes.COMMAND,
                ListServersAPI.COMMAND,
                LockMemberCommand.COMMAND,
                RenameChangePackage.COMMAND,
                SiConnectCommand.COMMAND,
                UnlockMemberCommand.COMMAND,
                ViewNonMembersCommand.COMMAND
        };
    }

    @Override
    @Nullable
    public CheckinEnvironment getCheckinEnvironment() {
        return mksCheckinEnvironment;
    }

    public SandboxCache getSandboxCache() {
        return sandboxCache;
    }

    @Override
    public void activate() {
        LOGGER.debug("activate [" + myProject + "]");
        try {
            URL resource = getClass().getResource("/" + "org.apache.commons.httpclient.MultiThreadedHttpConnectionManager".replace('.', '/') + ".class");
            if (!resource.toExternalForm().contains("/mksapi-without-commons-log")) {
                LOGGER.warn("not loading commons-http from mks jar : " + resource.toExternalForm());
            }
        } catch (Exception e) {
            LOGGER.error("failed locating commons-httpclient", e);
        }
        super.activate();
        final ChangeListManager changeListManager = ChangeListManager.getInstance(getProject());
        changeListManager.addChangeListListener(changeListAdapter);
        // the 2 cases need to be handled, as postStartup is not run if the vcs with the project after project loading
        // (eg when the user chooses a vcs for the project)
        if (!myProject.isInitialized()) {
            StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
                @Override
                public void run() {
                    postProjectLoadInit();
                }
            });
        } else {
            postProjectLoadInit();
        }
        myMessageBusConnection = getProject().getMessageBus().connect();
        myMessageBusConnection.subscribe(ProjectTopics.PROJECT_ROOTS, sandboxCache);
    }

    private void postProjectLoadInit() {
        if (!myProject.isDisposed()) {
/*
            final SandboxListSynchronizer synchronizer =
                    ApplicationManager.getApplication().getComponent(SandboxListSynchronizer.class);
            if (synchronizer == null) {
                LOGGER.error("SandboxSynchronizer applicationComponent is not running, MKS vcs will not be loaded");
                return;
            }
            myProject.getComponent(LongRunningTaskRepository.class).add(synchronizer);
            synchronizer.addListener(getSandboxCache());
*/
            initToolWindow();
        }
    }


    @Override
    public void deactivate() {
        try {
            final ChangeListManager changeListManager = ChangeListManager.getInstance(getProject());
            changeListManager.removeChangeListListener(changeListAdapter);
            sandboxCache.release();

            SandboxListSynchronizer component = ApplicationManager.getApplication().getComponent(SandboxListSynchronizer.class);
            if (component != null) {
                component.removeListener(getSandboxCache());
            }
            if (myMessageBusConnection != null) {
                myMessageBusConnection.disconnect();
            }
            unregisterToolWindow();
        } finally {
            super.deactivate();
        }
    }


    /**
     * used for the "Show History for Class/Method/Field/Selection..."
     * The action is not available if getVcsBlockHistoryProvider() returns
     * null.
     *
     * @return
     */
    @Override
    @Nullable
    public VcsHistoryProvider getVcsBlockHistoryProvider() {
        return getVcsHistoryProvider();
    }

    @Override
    @Nullable
    public VcsHistoryProvider getVcsHistoryProvider() {
        return vcsHistoryProvider;
    }

    @Override
    @Nullable
    public UpdateEnvironment getStatusEnvironment() {
        return updateEnvironment;
    }

    @Override
    public boolean isVersionedDirectory(final VirtualFile virtualFile) {
        // does not work currently as the vcs is not initialized yet ...
        final VirtualFile child = virtualFile.findChild(PROJECT_PJ_FILE);
        return null != child && child.exists();
    }

    @Override
    public boolean areDirectoriesVersionedItems() {
        return false;
    }

    @Override
    public VcsType getType() {
        return VcsType.centralized;
    }

    @Nullable
    @Override
    public RollbackEnvironment getRollbackEnvironment() {
        return rollbackEnvironment;
    }

    @Nullable
    @Override
    public AnnotationProvider getAnnotationProvider() {
        return new AnnotationProvider() {
            @Override
            public FileAnnotation annotate(VirtualFile file) throws VcsException {
                return annotate(file, null);
            }

            private AnnotateFileCommand createAnnotateCommand(VirtualFile file, ArrayList<VcsException> errors, VcsFileRevision revision) {
                return (revision == null)
                        ? new AnnotateFileCommand(errors, MksVcs.this, file.getCanonicalPath())
                        : new AnnotateFileCommand(errors, MksVcs.this, file.getCanonicalPath(), (MksRevisionNumber) revision.getRevisionNumber());
            }

            @Override
            public FileAnnotation annotate(VirtualFile file, VcsFileRevision revision) throws VcsException {
                ArrayList<VcsException> errors = new ArrayList<VcsException>();
                AnnotateFileCommand command = createAnnotateCommand(file, errors, revision);
                command.execute();

                ArrayList<VcsFileRevision> fileRevisions = new ArrayList<VcsFileRevision>();
                HashSet<VcsRevisionNumber> revisionSet = new HashSet<VcsRevisionNumber>();
                revisionSet.addAll(command.getRevisions());

                // collect commit info for the revisions involved
                ViewMemberHistoryAPICommand  memberHistoryCommand = new ViewMemberHistoryAPICommand (errors, MksVcs.this, file.getCanonicalPath());
                memberHistoryCommand.execute();
                List<MksMemberRevisionInfo> revisionsInfo = memberHistoryCommand.getRevisionsInfo();
                for (MksMemberRevisionInfo revisionInfo : revisionsInfo) {
                    if (revisionSet.contains(revisionInfo.getRevision())) {
                        fileRevisions.add(new MksVcsFileRevision(MksVcs.getInstance(myProject), VcsContextFactory.SERVICE.getInstance().createFilePathOn(file), revisionInfo));
                    }
                }

                return new MksFileAnnotation(myProject, file, command.getLineInfos(), command.getLines(), command.getRevisions(), fileRevisions);
            }

            @Override
            public boolean isAnnotationValid(VcsFileRevision rev) {
                return true;
            }
        };
    }

    @Override
    public Locale getDateLocale() {
        return ApplicationManager.getApplication().getComponent(MksConfiguration.class).getDateLocale();
    }
}
