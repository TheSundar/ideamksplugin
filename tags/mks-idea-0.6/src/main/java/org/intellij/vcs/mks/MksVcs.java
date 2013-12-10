package org.intellij.vcs.mks;

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
import javax.swing.*;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import org.intellij.vcs.mks.sicommands.GetContentRevision;
import org.intellij.vcs.mks.sicommands.ListChangePackageEntries;
import org.intellij.vcs.mks.sicommands.ListChangePackages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.EditFileProvider;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;

public class MksVcs extends AbstractVcs implements ProjectComponent, EncodingProvider {
    static final Logger LOGGER = Logger.getInstance(MksVcs.class.getName());
    public static final String TOOL_WINDOW_ID = "MKS";
    private ToolWindow mksToolWindow;
    private JTabbedPane mksContentPanel;
    private JTextPane mksTextArea;
    private MksVirtualFileAdapter myVirtualFileAdapter;
    static final boolean DEBUG = false;
    private static final int CHANGES_TAB_INDEX = 1;
    public static final String DATA_CONTEXT_PROJECT = "project";
    public static final String DATA_CONTEXT_MODULE = "module";
    public static final String DATA_CONTEXT_VIRTUAL_FILE_ARRAY = "virtualFileArray";
    private MKSChangeProvider myChangeProvider = new MKSChangeProvider(this);
    private final MksCheckinEnvironment mksCheckinEnvironment = new MksCheckinEnvironment(this);
    private final MksChangeListAdapter changeListAdapter = new MksChangeListAdapter(this);
    private final EditFileProvider editFileProvider = new _EditFileProvider(this);
    private final MksDiffProvider diffProvider = new MksDiffProvider(this);


    public MksVcs(Project project) {
        super(project);

    }

    @NotNull
    public String getComponentName() {
        return "MKS";
    }

    public void initComponent() {
        MKSHelper.startClient();
    }

    public void disposeComponent() {
    }

    @Override
    public Configurable getConfigurable() {
        return new MksConfigurableForm(myProject);
    }

    @Override
    public String getDisplayName() {
        return "MKS";
    }


    @Override
    public String getName() {
        return "MKS";
    }

    @Override
    public void start() throws VcsException {
        super.start();

        StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
            public void run() {
                initToolWindow();
            }
        });
    }

    public MksChangeListAdapter getChangeListAdapter() {
        return changeListAdapter;
    }

    @Override
    public void shutdown() throws VcsException {
        super.shutdown();
        unregisterToolWindow();
        MKSHelper.disconnect();
    }

    public void showErrors(java.util.List<VcsException> list, String action) {
        if (list.size() > 0) {
            StringBuffer buffer = new StringBuffer(mksTextArea.getText());
            buffer.append("\n");
            buffer.append(action).append(" Error: ");
            VcsException e;
            for (Iterator<VcsException> iterator = list.iterator(); iterator.hasNext(); buffer.append(e.getMessage()))
            {
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
        mksContentPanel.add(panelDebug, "Log", 0);
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

    private void maybeShowPopup(MouseEvent e, JPopupMenu menu) {
        if (e.isPopupTrigger()) {
            menu.show(mksTextArea, e.getX(), e.getY());
        }
    }

    private void unregisterToolWindow() {
        ToolWindowManager.getInstance(myProject).unregisterToolWindow("MKS");
        mksToolWindow = null;
    }

    public static MksVcs getInstance(Project project) {
        return project.getComponent(MksVcs.class);
    }

    @Override
    public synchronized boolean fileExistsInVcs(FilePath filePath) {
        if (DEBUG) {
            debug("fileExistsInVcs : " + filePath.getPresentableUrl());
        }
        try {

            TriclopsSiSandbox sandbox = MKSHelper.getSandbox(filePath.getVirtualFile(), this);
            TriclopsSiMembers members = MKSHelper.createMembers(sandbox);
            TriclopsSiMember triclopsSiMember = new TriclopsSiMember(filePath.getPresentableUrl());
            members.addMember(triclopsSiMember);
            try {
                MKSHelper.getMembersStatus(members);
            } catch (TriclopsException e) {
                throw new MksVcsException("can't get MKS status for [" + filePath.getPath() + "]\n" + getMksErrorMessage(), e);
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
     * @param filePath the file designation
     * @return true if the file is in a directory controlled by mks
     */
    @Override
    public synchronized boolean fileIsUnderVcs(FilePath filePath) {
        if (!filePath.getName().equals("project.pj")) {
            if (DEBUG) {
                debug("fileIsUnderVcs : " + filePath.getPresentableUrl());
            }
            try {
                MKSHelper.getSandbox(filePath.getVirtualFile(), this);
                return true;
            } catch (VcsException e) {
                ArrayList<VcsException> l = new ArrayList<VcsException>();
                l.add(e);
                showErrors(l, "fileExistsInVcs[" + filePath.getPath() + "]");
                return false;
            }
        } else {
            return false;
        }
    }

    private void debug(String s, Exception e) {
        StringBuffer oldText = new StringBuffer((mksTextArea == null) ? "" : mksTextArea.getText());
        oldText.append("\n").append(s);
        if (e != null) {
            LOGGER.info(s, e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            oldText.append(sw.toString());
        } else {
            LOGGER.info(s);
        }
        if (mksTextArea != null) {
            mksTextArea.setText(oldText.toString());
        }
    }

    @Override
    public DiffProvider getDiffProvider() {
        return diffProvider;
    }

    public static boolean isLastCommandCancelled() {
        return MKSHelper.isLastCommandCancelled();
    }

    /**
     * returns the module for
     *
     * @param child   the file we want to find the module
     * @param project the current project
     * @return the module if any, or null if none is found
     */
    @Nullable
    public static Module findModule(@NotNull Project project, @NotNull VirtualFile child) {
        // implementation for IDEA 5.1.x
        // see http://www.intellij.net/forums/thread.jspa?messageID=3311171&#3311171
        LOGGER.debug("findModule(project=" + project.getName() + ",file=" + child.getPresentableName() + ")");
        return ModuleUtil.findModuleForFile(child, project);

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

    public Project getProject() {
        return myProject;
    }

    @Override
    @NotNull
    public ChangeProvider getChangeProvider() {

        return myChangeProvider;
    }

    public String getMksSiEncoding(String command) {
        MksConfiguration configuration = ServiceManager.getService(myProject, MksConfiguration.class);
        Map<String, String> encodings = configuration.SI_ENCODINGS.getMap();
        return (encodings.containsKey(command)) ? encodings.get(command) : configuration.defaultEncoding;

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
                Messages.showErrorDialog("Unable to find the sandbox(es) for the file(s)", "Could Not Start checkout");
                return;
            }
            for (Map.Entry<TriclopsSiSandbox, ArrayList<VirtualFile>> entry : dispatchCommand.filesBySandbox.entrySet()) {
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

    public Map<TriclopsSiSandbox, ArrayList<VirtualFile>> dispatchBySandbox(VirtualFile[] files) {
        ArrayList<VcsException> dispatchErrors = new ArrayList<VcsException>();
        DispatchBySandboxCommand dispatchCommand = new DispatchBySandboxCommand(this, dispatchErrors, files);
        dispatchCommand.execute();
        return dispatchCommand.filesBySandbox;
    }

    public void projectOpened() {
        // todo MksVirtualFileAdapter
//		if (myVirtualFileAdapter == null) {
//			myVirtualFileAdapter = new MksVirtualFileAdapter(this);
//
//			VirtualFileManager.getInstance().addVirtualFileListener(myVirtualFileAdapter);
//		}
        ChangeListManager changeListManager = ChangeListManager.getInstance(getProject());
        changeListManager.addChangeListListener(changeListAdapter);
    }

    public void projectClosed() {
        if (myVirtualFileAdapter != null) {
            VirtualFileManager.getInstance().removeVirtualFileListener(myVirtualFileAdapter);
        }
        ChangeListManager changeListManager = ChangeListManager.getInstance(getProject());
        changeListManager.removeChangeListListener(changeListAdapter);

    }

    public static String[] getCommands() {
        return new String[]{
            GetContentRevision.COMMAND, ListChangePackageEntries.COMMAND, ListChangePackages.COMMAND
        };
    }

    @Override
    @Nullable
    public CheckinEnvironment getCheckinEnvironment() {
        return mksCheckinEnvironment;
    }
}
