                              package org.intellij.vcs.mks;

                              import com.intellij.openapi.actionSystem.ActionGroup;
                              import com.intellij.openapi.actionSystem.ActionManager;
                              import com.intellij.openapi.actionSystem.ActionPopupMenu;
                              import com.intellij.openapi.actionSystem.DataProvider;
                              import com.intellij.openapi.application.ApplicationManager;
                              import com.intellij.openapi.components.ProjectComponent;
                              import com.intellij.openapi.diagnostic.Logger;
                              import com.intellij.openapi.module.Module;
                              import com.intellij.openapi.options.Configurable;
                              import com.intellij.openapi.project.Project;
                              import com.intellij.openapi.roots.ProjectRootManager;
                              import com.intellij.openapi.ui.Messages;
                              import com.intellij.openapi.util.Computable;
                              import com.intellij.openapi.vcs.*;
                              import com.intellij.openapi.vcs.diff.DiffProvider;
                              import com.intellij.openapi.vfs.*;
                              import com.intellij.openapi.wm.ToolWindow;
                              import com.intellij.openapi.wm.ToolWindowAnchor;
                              import com.intellij.openapi.wm.ToolWindowManager;
                              import com.intellij.openapi.startup.StartupManager;
                              import mks.integrations.common.TriclopsException;
                              import mks.integrations.common.TriclopsSiMember;
                              import mks.integrations.common.TriclopsSiMembers;
                              import mks.integrations.common.TriclopsSiSandbox;
                              import org.jetbrains.annotations.NonNls;
                              import org.jetbrains.annotations.Nullable;

                              import javax.swing.*;
                              import javax.swing.text.StyleConstants;
                              import javax.swing.text.StyleContext;
                              import java.awt.*;
                              import java.awt.event.ActionEvent;
                              import java.awt.event.ActionListener;
                              import java.awt.event.MouseAdapter;
                              import java.awt.event.MouseEvent;
                              import java.util.*;
                              import java.util.List;

                              // Referenced classes of package org.intellij.vcs.mks:
                              //            MksConfigurable

                              public class MksVcs extends AbstractVcs implements ProjectComponent {
                              	private static final Logger LOGGER = Logger.getInstance(MksVcs.class.getName());
                              //	public static TriclopsSiClient CLIENT;
                              	public static final String TOOL_WINDOW_ID = "MKS";
                              	private static final int MAJOR_VERSION = 1;
                              	private static final int MINOR_VERSION = 1;
                              //	protected static final String CLIENT_LIBRARY_NAME = "mkscmapi";
                              	private ToolWindow mksToolWindow;
                              	private JTabbedPane mksContentPanel;
                              	private JTextPane mksTextArea;
                              	private MksVcs.MyVirtualFileAdapter myVirtualFileAdapter;
                              	private static final boolean DEBUG = false;
                              	private ChangedResourcesTableModel changedResourcesTableModel;
                              	private static final int CHANGES_TAB_INDEX = 1;
                              	public static final String DATA_CONTEXT_PROJECT = "project";
                              	public static final String DATA_CONTEXT_MODULE = "module";
                              	public static final String DATA_CONTEXT_VIRTUAL_FILE_ARRAY = "virtualFileArray";


                              	public MksVcs(Project project) {
                              		super(project);
                              		fileStatusProvider = new _FileStatusProvider(myProject);
                              	}

                              	public String getComponentName() {
                              		return "MKS";
                              	}

                              	public void initComponent() {
                              		try {
                              			start();
                              		} catch (VcsException e) {
                              			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                              		}
                              	}

                              	public void disposeComponent() {
                              	}

                              	public Configurable getConfigurable() {
                              		return new MksConfigurable(myProject);
                              	}

                              	public String getDisplayName() {
                              		return "MKS";
                              	}

                              //	public byte[] getFileContent(String path) throws VcsException {
                              //		return new byte[0];
                              //	}

                              	public String getName() {
                              		return "MKS";
                              	}

                              	public void start() throws VcsException {
                              		super.start();
                              		MKSHelper.startClient();
                              		StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
                              			public void run() {
                              				initToolWindow();
                              			}
                              		});

                              	}

                              	public void shutdown() throws VcsException {
                              		super.shutdown();
                              		unregisterToolWindow();
                              		MKSHelper.disconnect();
                              	}

                              	public void showErrors(java.util.List<VcsException> list, String action) {
                              		if (list.size() > 0) {
                              			StringBuffer buffer = new StringBuffer(mksTextArea.getText());
                              			buffer.append("\n");
                              			buffer.append(action + " Error: ");
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
                              		mksContentPanel = new JTabbedPane();
                              		//new JPanel(new java.awt.BorderLayout());
                              		mksTextArea = new JTextPane();
                              		mksTextArea.setEditable(false);
                              		javax.swing.text.Style def = StyleContext.getDefaultStyleContext().getStyle("default");
                              		javax.swing.text.Style regular = mksTextArea.addStyle("REGULAR", def);
                              		StyleConstants.setFontFamily(def, "SansSerif");
                              		javax.swing.text.Style s = mksTextArea.addStyle("ITALIC", regular);
                              		StyleConstants.setItalic(s, true);
                              		s = mksTextArea.addStyle("BOLD", regular);
                              		StyleConstants.setBold(s, true);

                              		JPanel panelDebug = new JPanel(new BorderLayout());
                              		mksContentPanel.add(panelDebug,"Log", 0);
                              		panelDebug.add(new JScrollPane(mksTextArea), BorderLayout.CENTER);

                              		mksToolWindow = toolWindowManager.registerToolWindow("MKS", mksContentPanel, ToolWindowAnchor.BOTTOM);
                              		java.net.URL iconUrl = getClass().getResource("/icons/mks.gif");
                              		javax.swing.Icon icn = new ImageIcon(iconUrl);
                              		mksToolWindow.setIcon(icn);
                              		final JPopupMenu menu = new JPopupMenu();
                              		JMenuItem item = new JMenuItem("Clear");
                              		item.addActionListener(new ActionListener() {
                              			public void actionPerformed(ActionEvent e) {
                              				mksTextArea.setText("");
                              			}
                              		});
                              		menu.add(item);
                              		mksTextArea.addMouseListener(new MouseAdapter() {
                              			public void mousePressed(MouseEvent e) {
                              				maybeShowPopup(e, menu);
                              			}

                              			public void mouseReleased(MouseEvent e) {
                              				maybeShowPopup(e, menu);
                              			}
                              		});

                              		class MyJTable extends JTable implements DataProvider {
                              			@Nullable
                              			/** returns the key properties for the table elements : project, module, the VirtualFile*/
                              			public Object getData(@NonNls String name) {
                              				if (DATA_CONTEXT_PROJECT.equals(name)) {
                              					return MksVcs.this.myProject;
                              				} else if (DATA_CONTEXT_MODULE.equals(name)) {
                              					Module aModule = findModule(MksVcs.this.myProject, changedResourcesTableModel.getVirtualFile(getSelectedRow()));
                              					return aModule;
                              				} else if (DATA_CONTEXT_VIRTUAL_FILE_ARRAY.equals(name)) {
                              					return new VirtualFile[]{changedResourcesTableModel.getVirtualFile(getSelectedRow())};
                              				}
                              //                System.out.println("ignoring data : "+name);
                              				return null;
                              			}
                              		}
                              		MyJTable changesTable = new MyJTable();
                              		changedResourcesTableModel = new ChangedResourcesTableModel();
                              		changesTable.setModel(changedResourcesTableModel);
                              		mksContentPanel.add(new JScrollPane(changesTable), "Changes", CHANGES_TAB_INDEX);
                              		final class MksChangePanelMouseAdapter extends MouseAdapter {
                              			public void mouseClicked(MouseEvent e) {
                              				if (MouseEvent.BUTTON3 == e.getButton() && e.getClickCount() == 1) {
                              					// Single right click
                              					ActionManager actionManager = ActionManager.getInstance();
                              					ActionGroup actionGroup = (ActionGroup) actionManager.getAction("MKS.Menu");
                              					ActionPopupMenu menu = actionManager.createActionPopupMenu("MksVcs.changes", actionGroup);
                              					JPopupMenu popupMenu = menu.getComponent();
                              					popupMenu.pack();
                              					popupMenu.show(e.getComponent(), e.getX(), e.getY());

                              					e.consume();
                              				}
                              			}
                              		}
                              		changesTable.addMouseListener(new MksChangePanelMouseAdapter());
                              	}

                              	private void maybeShowPopup(MouseEvent e, JPopupMenu menu) {
                              		if (e.isPopupTrigger()) {
                              			menu.show(mksTextArea, e.getX(), e.getY());
                              		}
                              	}

                              	private void unregisterToolWindow() {
                              		ToolWindowManager.getInstance(myProject).unregisterToolWindow("MKS");
                              		mksToolWindow = null;
                              	}

                              	public static AbstractVcs getInstance(Project project) {
                              		return (MksVcs) project.getComponent(org.intellij.vcs.mks.MksVcs.class);
                              	}

                              	public synchronized boolean fileExistsInVcs(FilePath filePath) {
                              		if (DEBUG) {
                              			debug("fileExistsInVcs : " + filePath.getPresentableUrl());
                              		}
                              		try {

                              			TriclopsSiSandbox sandbox = getSandbox(filePath.getVirtualFile());
                              			TriclopsSiMembers members = MKSHelper.createMembers(sandbox);
                              			TriclopsSiMember triclopsSiMember = new TriclopsSiMember(filePath.getPresentableUrl());
                              			members.addMember(triclopsSiMember);
                              			try {
                              				MKSHelper.getMembersStatus(members);
                              			} catch (TriclopsException e) {
                              				throw new VcsException("can't get MKS status for [" + filePath.getPath() + "]\n" + getMksErrorMessage());
                              			}
                              			TriclopsSiMember returnedMember = members.getMember(0);
                              			return returnedMember.isStatusControlled();
                              		} catch (VcsException e) {
                              			ArrayList<VcsException> l = new ArrayList<VcsException>();
                              			l.add(e);
                              			showErrors(l, "fileExistsInVcs for " + filePath.getPath());
                              			return false;
                              		}
                              	}

                              	public static synchronized String getMksErrorMessage() {
                              		return MKSHelper.getMksErrorMessage();
                              	}

                              	public void debug(String s) {
                              		debug(s, null);
                              	}

                              	/**
                              	 * checks if the file is in a directory controlled by mks
                              	 *
                              	 * @param filePath
                              	 * @return true if the file is in a directory controlled by mks
                              	 */
                              	public synchronized boolean fileIsUnderVcs(FilePath filePath) {
                              		if (filePath.getName().equals("project.pj")) return false;
                              		if (DEBUG) {
                              			debug("fileIsUnderVcs : " + filePath.getPresentableUrl());
                              		}
                              		try {
                              			TriclopsSiSandbox sandbox = getSandbox(filePath.getVirtualFile());
                              			return true;
                              		} catch (VcsException e) {
                              			ArrayList<VcsException> l = new ArrayList<VcsException>();
                              			l.add(e);
                              			showErrors(l, "fileExistsInVcs[" + filePath.getPath() + "]");
                              			return false;
                              		}
                              	}

                              	/**
                              	 * returns the mks sandbox in which the filepath is if any
                              	 *
                              	 * @param virtualFile
                              	 * @return
                              	 * @throws VcsException if the file is not in a sandbox
                              	 */
                              	public synchronized TriclopsSiSandbox getSandbox(VirtualFile virtualFile) throws VcsException {
                              		return MKSHelper.getSandbox(virtualFile);
                              	}

                              	private void debug(String s, Exception e) {
                              //		StringBuffer oldText = new StringBuffer(mksTextArea.getText());
                              //		oldText.append("\n").append(s);
                              //		if (e != null) {
                              //			StringWriter sw = new StringWriter();
                              //			PrintWriter pw = new PrintWriter(sw);
                              //			e.printStackTrace(pw);
                              //			oldText.append(sw.toString());
                              //		}
                              //		mksTextArea.setText(oldText.toString());
                              		if (e == null) LOGGER.info(s);
                              		else LOGGER.info(s, e);
                              	}

                              	public DiffProvider getDiffProvider() {
                              		// todo
                              		return super.getDiffProvider();
                              	}

                              	public static boolean isLastCommandCancelled() {
                              		return MKSHelper.isLastCommandCancelled();
                              	}

                              	public void setChanges(Map<VirtualFile, FileStatus> statuses) {
                              		this.changedResourcesTableModel.setFiles(statuses);
                              		mksContentPanel.setSelectedIndex(CHANGES_TAB_INDEX);
                              	}

                              	/**
                              	 * returns the module for
                              	 */
                              	public static Module findModule(Project project, VirtualFile child) {
                              		// implementation for IDEA 5.1.x
                              		// see http://www.intellij.net/forums/thread.jspa?messageID=3311171&#3311171
                              		assert project != null;
                              		assert child != null;
                              		LOGGER.debug("findModule(project="+project.getName()+",file="+child.getPresentableName()+")");
                              		return ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(child);
                              		//  for IDEA 6, could it also use same as 5.1.x ?
                              //		Module[] projectModules = ModuleManager.getInstance(project).getModules();
                              //		for (Module projectModule : projectModules) {
                              //			if (projectModule.getModuleScope().contains(child) /*|| projectModule.get*/) {
                              ////                System.out.println("found module " + projectModule.getName() + " for " + child);
                              //				return projectModule;
                              //			}
                              //			ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(projectModule);
                              //			for (VirtualFile contentRoot : moduleRootManager.getContentRoots()) {
                              //				if (child.getPath().startsWith(contentRoot.getPath())) {
                              ////                    System.out.println("" + child.getPath() + " is under module content root " + contentRoot.getPath());
                              //					return projectModule;
                              //				}
                              //			}
                              //		}
                              //		LOGGER.info("could not find module for " + child);
                              //		return null;
                              	}

                              //	private class _FileStatusProvider extends DelayedFileStatusProvider {
                              //		public _FileStatusProvider(Project project) {
                              //			super(project);
                              //		}
                              //
                              //		protected Map<VirtualFile, FileStatus> calcStatus(final Collection<VirtualFile> collection) {
                              //			return ApplicationManager.getApplication().runReadAction(new Computable<Map<VirtualFile, FileStatus>>() {
                              //				public Map<VirtualFile, FileStatus> compute() {
                              //					Map<VirtualFile, FileStatus> ret = new HashMap<VirtualFile, FileStatus>();
                              //					ArrayList<VcsException> dispatchErrors = new ArrayList<VcsException>();
                              //					DispatchBySandboxCommand dispatchCommand = new DispatchBySandboxCommand(dispatchErrors,
                              //							collection.toArray(new VirtualFile[collection.size()]));
                              //					dispatchCommand.execute();
                              //					for (VirtualFile virtualFile : dispatchCommand.notInSandboxFiles) {
                              //						ret.put(virtualFile, FileStatus.UNKNOWN);
                              //					}
                              //					for (Map.Entry<TriclopsSiSandbox, ArrayList<VirtualFile>> entry : dispatchCommand.filesBySandbox
                              //							.entrySet()) {
                              //						TriclopsSiSandbox sandbox = entry.getKey();
                              //						ArrayList<VirtualFile> files = entry.getValue();
                              //						ArrayList<VcsException> getStatusErrors = new ArrayList<VcsException>();
                              //						GetStatusesCommand getStatusesCommand = new GetStatusesCommand(MksVcs.this, getStatusErrors, sandbox, files);
                              //						getStatusesCommand.execute();
                              //						ret.putAll(getStatusesCommand.statuses);
                              //					}
                              //					return ret;
                              //				}
                              //			});
                              //		}
                              //	}

                              	private class _EditFileProvider implements EditFileProvider {
                              		public void editFiles(VirtualFile[] virtualFiles) throws VcsException {
                              			List<VcsException> errors = new ArrayList<VcsException>();
                              			DispatchBySandboxCommand dispatchCommand = new DispatchBySandboxCommand(errors, virtualFiles);
                              			dispatchCommand.execute();
                              			if (!dispatchCommand.errors.isEmpty()) {
                              				Messages.showErrorDialog("Unable to find the sandbox(es) for the file(s)", "Could Not Start checkout");
                              				return;
                              			}
                              			for (Map.Entry<TriclopsSiSandbox, ArrayList<VirtualFile>> entry : dispatchCommand.filesBySandbox.entrySet())
                              			{
                              				TriclopsSiSandbox sandbox = entry.getKey();
                              				ArrayList<VirtualFile> files = entry.getValue();
                              				errors = new ArrayList<VcsException>();
                              				CheckoutFilesCommand command = new CheckoutFilesCommand(errors, sandbox, files);
                              				synchronized (MksVcs.this) {
                              					command.execute();
                              				}
                              				if (!command.errors.isEmpty()) {
                              					Messages.showErrorDialog(errors.get(0).getLocalizedMessage(), "Could Not Start checkout");
                              					return;
                              				}
                              			}
                              		}

                              		public String getRequestText() {
                              			return "Would you like to invoke 'CheckOut' command?";
                              		}
                              	}

                              	private static class DispatchBySandboxCommand extends AbstractMKSCommand {
                              		private VirtualFile[] virtualFiles;
                              		protected Map<TriclopsSiSandbox, ArrayList<VirtualFile>> filesBySandbox =
                              				new HashMap<TriclopsSiSandbox, ArrayList<VirtualFile>>();
                              		protected ArrayList<VirtualFile> notInSandboxFiles = new ArrayList<VirtualFile>();

                              		public DispatchBySandboxCommand(List<VcsException> errors, VirtualFile[] virtualFiles) {
                              			super(errors);
                              			this.virtualFiles = virtualFiles;
                              		}

                              		public void execute() {
                              			Map<String, TriclopsSiSandbox> sandboxesByPath = new HashMap<String, TriclopsSiSandbox>();
                              			for (VirtualFile file : virtualFiles) {
                              				try {
                              					TriclopsSiSandbox sandbox = MKSHelper.getSandbox(file);
                              					TriclopsSiSandbox existingSandbox = sandboxesByPath.get(sandbox.getPath());
                              					if (existingSandbox == null) {
                              						existingSandbox = sandbox;
                              						sandboxesByPath.put(existingSandbox.getPath(), existingSandbox);
                              					}
                              					ArrayList<VirtualFile> managedFiles = filesBySandbox.get(existingSandbox);
                              					if (managedFiles == null) {
                              						managedFiles = new ArrayList<VirtualFile>();
                              						filesBySandbox.put(existingSandbox, managedFiles);
                              					}
                              					managedFiles.add(file);
                              				} catch (VcsException e) {
                              					LOGGER.debug("File not in sand box " + file.getPresentableUrl() + "\n" + getMksErrorMessage());
                              					notInSandboxFiles.add(file);
                              //					errors.add(new VcsException(e));
                              				}
                              			}
                              			if (DEBUG) {
                              				LOGGER.debug("dispatched " + virtualFiles.length + " files to " + filesBySandbox.size() + " sandboxes");
                              			}
                              		}
                              	}

                              	private static abstract class AbstractMKSCommand {
                              		protected List<VcsException> errors;

                              		public AbstractMKSCommand(List<VcsException> errors) {
                              			this.errors = errors;
                              		}

                              		public abstract void execute();

                              //		protected TriclopsSiSandbox getSandbox(VirtualFile virtualFile) throws TriclopsException {
                              //			if (!MksVcs.isValid()) {
                              //				MksVcs.startClient();
                              //			}
                              //			TriclopsSiSandbox sandbox = new TriclopsSiSandbox(MksVcs.CLIENT);
                              //			sandbox.setIdeProjectPath(virtualFile.getPresentableUrl());
                              //			sandbox.validate();
                              //			return sandbox;
                              //		}
                              //

                              		protected TriclopsSiMembers prepareMembers(ArrayList<VirtualFile> files, TriclopsSiSandbox sandbox) throws TriclopsException {
                              			TriclopsSiMembers members = MKSHelper.createMembers(sandbox);
                              			for (VirtualFile virtualFile : files) {
                              				members.addMember(new TriclopsSiMember(virtualFile.getPresentableUrl()));
                              			}
                              			MKSHelper.getMembersStatus(members);
                              			return members;
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

                              		public void execute() {
                              			try {
                              				TriclopsSiMembers members = prepareMembers(files, sandbox);
                              				MKSHelper.checkoutMembers(members, 0);
                              			} catch (TriclopsException e) {
                              				errors.add(new VcsException("unable to checkout" + "\n" + getMksErrorMessage()));
                              			}
                              		}

                              	}

                              	private final EditFileProvider editFileProvider = new _EditFileProvider();

                              	@Nullable
                              	public EditFileProvider getEditFileProvider() {
                              		return editFileProvider;
                              	}

                              	private class MyVirtualFileAdapter extends VirtualFileAdapter {
                              		public MyVirtualFileAdapter() {
                              			super();	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("new MyVirtualFileAdapter");
                              		}

                              		public void propertyChanged(VirtualFilePropertyEvent event) {
                              			super.propertyChanged(event);	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("propertyChanged[" + event + "]");
                              		}

                              		public void contentsChanged(VirtualFileEvent virtualFileEvent) {
                              			super.contentsChanged(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("contentsChanged[" + virtualFileEvent + "]");
                              		}

                              		public void beforeContentsChange(VirtualFileEvent virtualFileEvent) {
                              			super.beforeContentsChange(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("beforeContentsChange[" + virtualFileEvent + "]");
                              		}

                              		public void beforeFileDeletion(VirtualFileEvent virtualFileEvent) {
                              			super.beforeFileDeletion(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("beforeFileDeletion[" + virtualFileEvent + "]");
                              		}

                              		public void fileCreated(VirtualFileEvent virtualFileEvent) {
                              			super.fileCreated(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("fileCreated[" + virtualFileEvent + "]");
                              		}

                              		public void fileDeleted(VirtualFileEvent virtualFileEvent) {
                              			super.fileDeleted(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("fileDeleted[" + virtualFileEvent + "]");
                              		}

                              		public void beforePropertyChange(VirtualFilePropertyEvent event) {
                              			super.beforePropertyChange(event);	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("beforePropertyChange[" + event + "]");
                              //			if (event.getPropertyName().equalsIgnoreCase(VirtualFile.PROP_NAME)) {
                              //				final VirtualFile file = event.getFile();
                              //				final VirtualFile parent = file.getParent();
                              //				try {
                              //					try {
                              //						TriclopsSiSandbox sandbox = null;
                              //						sandbox = getSandbox(file);
                              //						TriclopsSiMembers members = new TriclopsSiMembers(MksVcs.CLIENT, sandbox);
                              //
                              //						members.addMember(new TriclopsSiMember(file.getPresentableUrl()));
                              //						members.addMember(new TriclopsSiMember(file.getParent().getPresentableUrl()+"\\"+event.getNewValue()));
                              //
                              //						members.renameMember(TriclopsSiMembers.SI_RENAME_MEMBER_CONFIRM);
                              //					} catch (TriclopsException e) {
                              //						debug("rename member [" + file.getPath() + "] to [" + event.getNewValue() + "] failed", e);
                              //						throw new VcsException("rename member [" + file.getPath() + "] to [" + event.getNewValue() + "] failed");
                              //					}
                              //				} catch (VcsException e) {
                              //					debug(e.getMessage(), e);
                              //				}
                              //			}
                              		}

                              		public void fileMoved(VirtualFileMoveEvent virtualFileMoveEvent) {
                              			super.fileMoved(virtualFileMoveEvent);	//To change body of overridden methods use File | Settings | File Templates.
                              			debug("fileMoved[" + virtualFileMoveEvent + "]");
                              		}

                              		public void beforeFileMovement(VirtualFileMoveEvent virtualFileMoveEvent) {
                              			super.beforeFileMovement(virtualFileMoveEvent);
                              			debug("beforeFileMovement[" + virtualFileMoveEvent + "]");
                              		}
                              	}

                              	public static class GetStatusesCommand extends AbstractMKSCommand {
                              		private MksVcs mksvcs;
                              		private final TriclopsSiSandbox sandbox;
                              		private final ArrayList<VirtualFile> files;
                              		public final HashMap<VirtualFile, FileStatus> statuses;

                              		public GetStatusesCommand(MksVcs mksvcs, ArrayList<VcsException> errors, TriclopsSiSandbox sandbox,
                              								  ArrayList<VirtualFile> files) {
                              			super(errors);
                              			this.mksvcs = mksvcs;
                              			this.sandbox = sandbox;
                              			this.files = files;
                              			statuses = new HashMap<VirtualFile, FileStatus>();
                              		}

                              		public void execute() {
                              			long deb = System.currentTimeMillis();
                              			try {
                              				// todo begin
                              				String[] projectMembers = new String[0];
                              				// la liste des fichiers qu'on va envoyer a mks
                              				ArrayList<VirtualFile> files2 = new ArrayList<VirtualFile>();
                              				try {
                              					projectMembers = MKSHelper.getProjectMembers(sandbox);
                              				} catch (TriclopsException e) {
                              					LOGGER.info("could not get project members for sandbox : " + sandbox.getPath() + ", project:" + sandbox.getSandboxProject());
                              					projectMembers = null;
                              				}
                              				if (projectMembers != null) {
                              					// le virtual file du project.pj de la sandbox
                              					VirtualFile projectPjVFile = files.get(0).getFileSystem().findFileByPath(sandbox.getPath().replace('\\', '/'));
                              					// le repertoire de la sandbox
                              					VirtualFile sandboxFolder = projectPjVFile.getParent();
                              					if (sandboxFolder == null) {
                              						LOGGER.info("cant find sandbox folder)");
                              						return;
                              					}
                              					// les repertoires faisant partie du projet (obtenu via le SiProject)
                              					HashSet<VirtualFile> projectFolders = new HashSet<VirtualFile>();
                              					projectFolders.add(sandboxFolder);
                              					for (String projectMemberRelativePath : projectMembers) {
                              						int lastIndexOfSlash = projectMemberRelativePath.lastIndexOf('/');
                              						if (lastIndexOfSlash >= 0) {
                              							VirtualFile projectFolder = sandboxFolder.findFileByRelativePath(projectMemberRelativePath.substring(0, lastIndexOfSlash));
                              							if (projectFolder != null && !projectFolders.contains(projectFolder)) {
                              								if (LOGGER.isDebugEnabled()) {
                              									LOGGER.debug("detected project folder :" + projectFolder);
                              								}
                              								boolean found = false;
                              								// add all parent folders : takes care of parent folders that do not have any file children (only directories)
                              								do {
                              									projectFolders.add(projectFolder);
                              									projectFolder = projectFolder.getParent();
                              								}
                              								while (projectFolder != null && !projectFolders.contains(projectFolder) && !projectFolder.equals(sandboxFolder));
                              							} else if (projectFolder == null) {
                              								LOGGER.debug("can't find folder for path " + projectMemberRelativePath);
                              							}
                              						}
                              					}

                              					//  les repertoires pour lesques mks connait des enfants => donc les connait aussi
                              //				ArrayList<VirtualFile> knownDirs = new ArrayList<VirtualFile>();
                              					//  pour eviter de demander a mks les status de tous les fichiers ignores, on enleve de files tous ceux
                              					// qui ne sont pas dans un repertoire faisant partie du projet
                              					for (VirtualFile virtualFile : files) {
                              						if (!virtualFile.isDirectory()) {
                              							if (projectFolders.contains(virtualFile.getParent())) {
                              								// dans un repertoire connu de mks
                              								files2.add(virtualFile);
                              //							if (!knownDirs.contains(virtualFile.getParent())) {
                              //								knownDirs.add(virtualFile.getParent());
                              //							}
                              							} else {
                              //						mksvcs.debug(virtualFile.getPath()+" does not seem to be part of the sandbox "+sandbox.getPath());
                              								statuses.put(virtualFile, FileStatus.UNKNOWN);
                              							}
                              						} else {
                              							statuses.put(virtualFile, (projectFolders.contains(virtualFile)) ? FileStatus.NOT_CHANGED : FileStatus.UNKNOWN);
                              						}
                              					}
                              					if (LOGGER.isDebugEnabled()) {
                              						LOGGER.debug("locally resolved " + statuses.size() + "n querying mks for " + files2.size());
                              					}
                              					// a ce point files2 contient uniquement les fichiers controllés par mks (ou fils direct d'un des dossiers du projet au moins)
                              				} else {
                              					files2 = files;
                              				}
                              				// todo end
                              				TriclopsSiMembers members = prepareMembers(files2, sandbox);

                              				for (int i = 0; i < members.getNumMembers(); i++) {
                              					FileStatus status = FileStatus.UNKNOWN;
                              					TriclopsSiMember member = members.getMember(i);
                              					// directories status is not available in mks
                              					if (files2.get(i).isDirectory()) {
                              						status = FileStatus.NOT_CHANGED;
                              					} else if (member.isStatusKnown() && member.isStatusNotControlled()) {
                              						status = FileStatus.UNKNOWN;
                              					} else if (member.isStatusKnown() && member.isStatusControlled()) {
                              						if (member.isStatusNoWorkingFile()) {
                              							status = FileStatus.DELETED_FROM_FS;
                              						} else if (member.isStatusDifferent()) {
                              							status = FileStatus.MODIFIED;
                              						} else if (!member.isStatusDifferent()) {
                              							status = FileStatus.NOT_CHANGED;
                              						}
                              					}
                              					if (DEBUG) {
                              						mksvcs.debug("status " + member.getPath() + "==" + status);
                              					}
                              					statuses.put(files2.get(i), status);
                              				}
                              			} catch (TriclopsException e) {
                              				errors.add(new VcsException("unable to get status" + "\n" + getMksErrorMessage()));
                              			}
                              			long end = System.currentTimeMillis();
                              			if (LOGGER.isDebugEnabled()) {
                              				LOGGER.debug("got statuses for " + sandbox.getPath() + " in " + (end - deb));
                              			}

                              		}
                              	}
                              	public static Map<TriclopsSiSandbox, ArrayList<VirtualFile>> dispatchBySandbox(VirtualFile[] files) {
                              		ArrayList<VcsException> dispatchErrors = new ArrayList<VcsException>();
                              		DispatchBySandboxCommand dispatchCommand = new DispatchBySandboxCommand(dispatchErrors,files);
                              		dispatchCommand.execute();
                              		return dispatchCommand.filesBySandbox;
                              	}

                              	public static class CalcStatusComputable implements Computable<Map<VirtualFile, FileStatus>> {
                              		private MksVcs mksvcs;
                              		private final Collection<VirtualFile> collection;

                              		public CalcStatusComputable(MksVcs mksvcs, Collection<VirtualFile> collection) {
                              			this.mksvcs = mksvcs;
                              			this.collection = collection;
                              		}

                              		public Map<VirtualFile, FileStatus> compute() {
                              			Map<VirtualFile, FileStatus> ret = new HashMap<VirtualFile, FileStatus>();
                              			ArrayList<VcsException> dispatchErrors = new ArrayList<VcsException>();
                              			DispatchBySandboxCommand dispatchCommand = new DispatchBySandboxCommand(dispatchErrors,
                              					collection.toArray(new VirtualFile[collection.size()]));
                              			long deb = System.currentTimeMillis();
                              			dispatchCommand.execute();
                              			long end = System.currentTimeMillis();
                              			if (LOGGER.isDebugEnabled()) {
                              				LOGGER.debug("dispatched " + collection.size() + " in " + (end - deb));
                              			}
                              			for (VirtualFile virtualFile : dispatchCommand.notInSandboxFiles) {
                              				ret.put(virtualFile, FileStatus.UNKNOWN);
                              			}
                              			deb = System.currentTimeMillis();
                              			for (Map.Entry<TriclopsSiSandbox, ArrayList<VirtualFile>> entry : dispatchCommand.filesBySandbox
                              					.entrySet()) {
                              				TriclopsSiSandbox sandbox = entry.getKey();
                              				ArrayList<VirtualFile> files = entry.getValue();

                              				ArrayList<VcsException> getStatusErrors = new ArrayList<VcsException>();
                              				GetStatusesCommand getStatusesCommand = new GetStatusesCommand(CalcStatusComputable.this.mksvcs, getStatusErrors, sandbox, files);
                              				getStatusesCommand.execute();
                              				ret.putAll(getStatusesCommand.statuses);
                              			}
                              			end = System.currentTimeMillis();
                              			if (LOGGER.isDebugEnabled()) {
                              				LOGGER.debug("got statuses for " + collection.size() + " in " + (end - deb));
                              			}

                              			return ret;
                              		}
                              	}

                              	/**
                              	 * pour IDEA 6
                              	 */
                              	private class _FileStatusProvider implements FileStatusProvider {
                              		private final Project project;

                              		public _FileStatusProvider(Project project) {
                              			this.project = project;
                              		}


                              		public FileStatus getStatus(VirtualFile virtualFile) {
                              			ArrayList<VirtualFile> collection = new ArrayList<VirtualFile>();
                              			collection.add(virtualFile);

                              			Map<VirtualFile, FileStatus> map = ApplicationManager.getApplication().runReadAction(new CalcStatusComputable(MksVcs.this, collection));
                              			FileStatus fileStatus = map.get(virtualFile);
                              			LOGGER.info("status for : " + virtualFile + " = " + fileStatus);

                                          return fileStatus;
                              /*			return fileStatus;
                                          if (!PerforceSettings.getSettings(myProject).ENABLED) return FileStatus.NOT_CHANGED;
                                          if (PerforceManager.getInstance(myProject).isUnderPerforceRoot(virtualFile)) {
                                            if (!virtualFile.isDirectory()) {
                                              final FStat fStat;
                                              try {
                                                final P4File p4File = P4File.create(virtualFile);
                                                fStat = p4File.getFstat(PerforceSettings.getSettings(myProject),
                                                                        PerforceConnectionManager.getInstance(myProject).getConnectionForFile(virtualFile), false);
                                              }
                                              catch (VcsException e) {
                                                return FileStatus.UNKNOWN;
                                              }

                                              return convertToFileStatus(fStat);
                                            }
                                            else {
                                              return FileStatus.NOT_CHANGED;
                                            }
                                          }
                                          else {
                                            return FileStatus.UNKNOWN;
                                          }
                              */
                               }
                              /*
                                      protected Map<VirtualFile, FileStatus> calcStatus(final Collection<VirtualFile> collection) {
                                          return ApplicationManager.getApplication().runReadAction(new CalcStatusComputable(MksVcs.this, collection));
                                      }
                              */
                              	}

                              	private final FileStatusProvider fileStatusProvider;

                              	public FileStatusProvider getFileStatusProvider() {
                              		return fileStatusProvider;

                              //		return LocalVcsServices.getInstance(myProject).getFileStatusProvider();
                              	}

                              	public void projectOpened() {
                              		myVirtualFileAdapter = new MyVirtualFileAdapter();
                              		// todo desactivé
                              //		VirtualFileManager.getInstance().addVirtualFileListener(myVirtualFileAdapter);
                              	}

                              	public void projectClosed() {
                              		if (myVirtualFileAdapter != null) {
                              			VirtualFileManager.getInstance().removeVirtualFileListener(myVirtualFileAdapter);
                              		}
                              	}

                              //	private static class MksHistoryProvider implements VcsHistoryProvider {
                              //		public HistoryAsTreeProvider getTreeHistoryProvider() {
                              //			return null;
                              //		}
                              //
                              //		public ColumnInfo[] getRevisionColumns() {
                              //			return new ColumnInfo[0];
                              //		}
                              //
                              //		public AnAction[] getAdditionalActions(final FileHistoryPanel panel) {
                              //			return new AnAction[0];  //To change body of implemented methods use File | Settings | File Templates.
                              //		}
                              //
                              //		@NonNls
                              //		public String getHelpId() {
                              //			return null;
                              //		}
                              //
                              //		public VcsHistorySession createSessionFor(FilePath filePath) throws VcsException {
                              //			return new VcsHistorySession() {
                              //				@Nullable
                              //				protected VcsRevisionNumber calcCurrentRevisionNumber() {
                              //
                              //				}
                              //			};
                              //		}
                              //	}
                              }
