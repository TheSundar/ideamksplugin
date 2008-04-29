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
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.update.UpdateEnvironment;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.AbstractTableCellEditor;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.history.MksVcsHistoryProvider;
import org.intellij.vcs.mks.realtime.*;
import org.intellij.vcs.mks.sicommands.*;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MksVcs extends AbstractVcs implements MksCLIConfiguration {
	static final Logger LOGGER = Logger.getInstance(MksVcs.class.getName());
	public static final String TOOL_WINDOW_ID = "MKS";
	static final boolean DEBUG = false;
	public static final String DATA_CONTEXT_PROJECT = "project";
	public static final String DATA_CONTEXT_MODULE = "module";
	public static final String DATA_CONTEXT_VIRTUAL_FILE_ARRAY = "virtualFileArray";

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
	private static final String MKS_TOOLWINDOW = "MKS";

	public MksVcs(Project project) {
		super(project);
		sandboxCache = new SandboxCacheImpl(project);
	}

	@Override
	public Configurable getConfigurable() {
		return new MksConfigurableForm(ApplicationManager.getApplication().getComponent(MksConfiguration.class));
	}

	@Override
	public String getDisplayName() {
		return VCS_NAME;
	}


	@Override
	public String getName() {
		return VCS_NAME;
	}

	public MksChangeListAdapter getChangeListAdapter() {
		return changeListAdapter;
	}

	public void showErrors(java.util.List<VcsException> list, String action) {
		if (!list.isEmpty()) {
			StringBuffer buffer = new StringBuffer(mksTextArea.getText());
			buffer.append("\n");
			buffer.append(action).append(" Error: ");
			VcsException e;
			for (Iterator<VcsException> iterator = list.iterator(); iterator.hasNext(); buffer.append(e.getMessage())) {
				e = iterator.next();
				buffer.append("\n");
			}
			mksTextArea.setText(buffer.toString());
		}
	}

	private void initToolWindow() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
		JToolBar toolbar = new JToolBar();
		Action viewSandboxesAction = new AbstractAction("view sandboxes") {
			public void actionPerformed(final ActionEvent event) {
				StringWriter stringWriter = new StringWriter();
				PrintWriter pw = new PrintWriter(stringWriter);
				pw.println(mksTextArea.getText());
				sandboxCache.dumpStateOn(pw);
				mksTextArea.setText(stringWriter.toString());
			}
		};
		toolbar.add(viewSandboxesAction);
		JPanel mksPanel = new JPanel(new BorderLayout());
		mksPanel.add(toolbar, BorderLayout.NORTH);
		JTabbedPane tabbedPane = new JTabbedPane();

		this.mksTextArea = createMksLogTextPane();
		JPanel panelDebug = new JPanel(new BorderLayout());
		panelDebug.add(new JScrollPane(mksTextArea), BorderLayout.CENTER);
		tabbedPane.add(panelDebug, "Log", 0);


		tabbedPane.add(createTasksPanel(), "Daemon processes");
		mksPanel.add(tabbedPane, BorderLayout.CENTER);
		registerToolWindow(toolWindowManager, mksPanel);
		final JPopupMenu menu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Clear");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mksTextArea.setText("");
			}
		});
		menu.add(item);
		mksTextArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e, menu);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
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
			LongRunningTask task = myProject.getComponent(LongRunningTaskRepository.class).get(row);
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
		JTable tasksTable = new JTable();

		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BorderLayout());
		JPanel buttonsPanel = new JPanel();
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
					public void actionPerformed(ActionEvent e) {
						System.err.println("restarting sandbox list listener");
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

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, final int row,
														 int column) {
				JButton button = new JButton("restart");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent event) {
						LongRunningTask task = myProject.getComponent(LongRunningTaskRepository.class).get(row);
						LOGGER.debug("restarting task " + task);
						task.restart();
					}
				});
				return button;
			}
		});
		return jPanel;

	}

	private JTextPane createMksLogTextPane() {
		JTextPane mksTextArea = new JTextPane();
		mksTextArea.setEditable(false);
		javax.swing.text.Style def = StyleContext.getDefaultStyleContext().getStyle("default");
		javax.swing.text.Style regular = mksTextArea.addStyle("REGULAR", def);
		StyleConstants.setFontFamily(def, "SansSerif");
		javax.swing.text.Style s = mksTextArea.addStyle("ITALIC", regular);
		StyleConstants.setItalic(s, true);
		s = mksTextArea.addStyle("BOLD", regular);
		StyleConstants.setBold(s, true);
		return mksTextArea;
	}

	private ToolWindow registerToolWindow(final ToolWindowManager toolWindowManager, final JPanel mksPanel) {
		ToolWindow toolWindow = toolWindowManager.registerToolWindow(MKS_TOOLWINDOW, true, ToolWindowAnchor.BOTTOM);
		PeerFactory pf = com.intellij.peer.PeerFactory.getInstance();
		Content content = pf.getContentFactory().createContent(mksPanel, "", false); // first arg is a JPanel
		content.setCloseable(false);
		toolWindow.getContentManager().addContent(content);

		toolWindow.setIcon(IconLoader.getIcon("/icons/mks.gif", getClass()));
		return toolWindow;
	}

	private void maybeShowPopup(MouseEvent e, JPopupMenu menu) {
		if (e.isPopupTrigger()) {
			menu.show(mksTextArea, e.getX(), e.getY());
		}
	}

	private void unregisterToolWindow() {
		ToolWindowManager.getInstance(myProject).unregisterToolWindow("MKS");
	}

	public static MksVcs getInstance(Project project) {
		return (MksVcs) ProjectLevelVcsManager.getInstance(project).findVcsByName(VCS_NAME);
	}

	public static synchronized String getMksErrorMessage() {
		return MKSHelper.getMksErrorMessage();
	}

	public void debug(String s) {
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
	public synchronized boolean fileIsUnderVcs(FilePath filePath) {
//		System.out.println("super.fileIsUnderVcs " + filePath + " = " + super.fileIsUnderVcs(filePath));
		return super.fileIsUnderVcs(filePath)
				&& !getSandboxCache().isSandboxProject(filePath.getVirtualFile());
	}

	private void debug(String s, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
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

	public static boolean isLastCommandCancelled() {
		return MKSHelper.isLastCommandCancelled();
	}

	public Project getProject() {
		return myProject;
	}

	@Override
	@NotNull
	public ChangeProvider getChangeProvider() {
		return myProject.getComponent(MKSChangeProvider.class);
	}

	@NotNull
	public String getMksSiEncoding(String command) {
		return ApplicationManager.getApplication().getComponent(MksConfiguration.class).getMksSiEncoding(command);
	}

	@NotNull
	public String getDatePattern() {
		return ApplicationManager.getApplication().getComponent(MksConfiguration.class).getDatePattern();
	}

	private class _EditFileProvider implements EditFileProvider {
		private final MksVcs mksVcs;

		public _EditFileProvider(MksVcs mksVcs) {
			this.mksVcs = mksVcs;
		}

		public void editFiles(VirtualFile[] virtualFiles) throws VcsException {
			List<VcsException> errors = new ArrayList<VcsException>();
			DispatchBySandboxCommand dispatchCommand = new DispatchBySandboxCommand(mksVcs, errors, virtualFiles);
			dispatchCommand.execute();
			if (!dispatchCommand.errors.isEmpty()) {
				Messages.showErrorDialog(MksBundle.message("unable.to.find.the.sandboxes.for.the.files.title"),
						MksBundle.message("could.not.start.checkout"));
				return;
			}
			for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : dispatchCommand.filesBySandbox.entrySet()) {
				MksSandboxInfo sandbox = entry.getKey();
				ArrayList<VirtualFile> files = entry.getValue();
				errors = new ArrayList<VcsException>();
				CheckoutFilesCommand command = new CheckoutFilesCommand(errors, sandbox.getSiSandbox(), files);
				synchronized (MksVcs.this) {
					command.execute();
				}
				if (!command.errors.isEmpty()) {
					Messages.showErrorDialog(errors.get(0).getLocalizedMessage(),
							MksBundle.message("could.not.start.checkout"));
					return;
				}
			}
		}

		public String getRequestText() {
			return MksBundle.message("edit.file.provider.request.text");
		}
	}

	private static class CheckoutFilesCommand extends AbstractMKSCommand {
		private TriclopsSiSandbox sandbox;
		private ArrayList<VirtualFile> files;

		public CheckoutFilesCommand(List<VcsException> errors, TriclopsSiSandbox sandbox,
									ArrayList<VirtualFile> files) {
			super(errors);
			this.sandbox = sandbox;
			this.files = files;
		}

		@Override
		public void execute() {
			try {
				TriclopsSiMembers members = queryMksMemberStatus(files, sandbox);
				MKSHelper.checkoutMembers(members, 0);
			} catch (TriclopsException e) {
				//noinspection ThrowableInstanceNeverThrown
				errors.add(new MksVcsException("unable to checkout" + "\n" + getMksErrorMessage(), e));
			}
		}

	}

	@Override
	@Nullable
	public EditFileProvider getEditFileProvider() {
		return editFileProvider;
	}

	public Map<MksSandboxInfo, ArrayList<VirtualFile>> dispatchBySandbox(VirtualFile[] files) {
		ArrayList<VcsException> dispatchErrors = new ArrayList<VcsException>();
		DispatchBySandboxCommand dispatchCommand = new DispatchBySandboxCommand(this, dispatchErrors, files);
		dispatchCommand.execute();
		return dispatchCommand.filesBySandbox;
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
				ListServers.COMMAND,
				LockMemberCommand.COMMAND,
				RenameChangePackage.COMMAND,
				SiConnectCommand.COMMAND,
				UnlockMemberCommand.COMMAND,
				ViewMemberHistoryCommand.COMMAND,
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
		super.activate();
		ChangeListManager changeListManager = ChangeListManager.getInstance(getProject());
		changeListManager.addChangeListListener(changeListAdapter);
		// the 2 cases need to be handled, as postStartup is not run if the vcs with the project after project loading
		// (eg when the user chooses a vcs for the project)
		if (!myProject.isInitialized()) {
			StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
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
			final SandboxListSynchronizer synchronizer =
					ApplicationManager.getApplication().getComponent(SandboxListSynchronizer.class);
			if (synchronizer == null) {
				LOGGER.error("SandboxSynchronizer applicationComponent is not running, MKS vcs will not be loaded");
				return;
			}
			myProject.getComponent(LongRunningTaskRepository.class).add(synchronizer);
			synchronizer.addListener(getSandboxCache());
			VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
			initToolWindow();
		}
	}


	@Override
	public void deactivate() {
		ChangeListManager changeListManager = ChangeListManager.getInstance(getProject());
		changeListManager.removeChangeListListener(changeListAdapter);
		sandboxCache.release();

		ApplicationManager.getApplication().getComponent(SandboxListSynchronizer.class)
				.removeListener(getSandboxCache());
		if (myMessageBusConnection != null) {
			myMessageBusConnection.disconnect();
		}
		unregisterToolWindow();
		super.deactivate();
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
}
