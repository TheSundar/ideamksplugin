package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksConfiguration;
import org.intellij.vcs.mks.MksVcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BasicAction extends AnAction {
    protected MksConfiguration configuration;
    private static final Logger LOG = Logger.getInstance("#org.intellij.vcs.mks.BasicAction");
    protected static final String ACTION_CANCELLED_MSG = "The command was cancelled.";

    public BasicAction() {
    }

    /**
     * exit if no project or no module
     *
     * @param event
     */
    public void actionPerformed(AnActionEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("enter: actionPerformed(id='" + ActionManager.getInstance().getId(this) + "')");
        }
        final DataContext dataContext = event.getDataContext();
        final Project project = (Project) dataContext.getData(MksVcs.DATA_CONTEXT_PROJECT);
//		final Module module = (Module) dataContext.getData(MksVcs.DATA_CONTEXT_MODULE);
        if (project == null) {
            return;
        }
//		configuration = project.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
//		if (configuration == null) {
//			configuration = module.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
//		}
//		if (configuration == null) {
//			LOG.error("could not find MKS configuration for [project="+project.getName()+",module ="+module.getName()+"]");
//		}
        final VirtualFile[] files = (VirtualFile[]) dataContext.getData(MksVcs.DATA_CONTEXT_VIRTUAL_FILE_ARRAY);
        if (LOG.isDebugEnabled()) {
            LOG.debug("files='" + Arrays.asList(files) + "'");
        }
        if (files == null || files.length == 0) {
            return;
        }
        FileDocumentManager.getInstance().saveAllDocuments();
        final AbstractVcs vcs = findConcernedVcs(project, files);
        if (vcs == null) {
            LOG.debug("no VCS enabled resources concerned by this action, returning");
            return;
        }
        String actionName = getActionName(vcs);
        // does not work if MKS is not selected as the project VCS !!
        AbstractVcsHelper helper = AbstractVcsHelper.getInstance(project);
        com.intellij.openapi.localVcs.LvcsAction action = helper.startVcsAction(actionName);
        List<VcsException> exceptions = helper.runTransactionRunnable(vcs, new TransactionRunnable() {
            public void run(List exceptions) {
                execute(project, files, exceptions, dataContext);
            }

        }, null);
        MksVcs mksVcs = (MksVcs) vcs;
        mksVcs.showErrors(exceptions, actionName != null ? actionName : vcs.getDisplayName());
        if (actionName != null)
            helper.finishVcsAction(action);
    }

    private AbstractVcs findConcernedVcs(Project project, VirtualFile[] files) {
//	    VcsManager manager = VcsManager.getInstance(project);
        AbstractVcs vcs = null; // manager.getActiveVcs();
        if (vcs == null) {
            LOG.info("no project level defined VCS, using the first file one");
            for (VirtualFile file : files) {
                vcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(file);
                if (vcs == null) {
                    break;
                }
            }
        }
        return vcs;
    }

    /**
     * disable the action if the event doest not apply on MksVcs enabled resources
     * Hide it if the event does not have a project, or if no VirtualFiel are targeted
     *
     * @param e
     */
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        DataContext dataContext = e.getDataContext();
        Project project = (Project) dataContext.getData(MksVcs.DATA_CONTEXT_PROJECT);
        if (project == null) {
            presentation.setEnabled(false);
            presentation.setVisible(false);
            return;
        }
        // was accurate when 1 project => 1 maximum VCS, outdated now that you have module level VCS
        //manager.getActiveVcs();
//		VcsManager manager = VcsManager.getInstance(project);
//		if (manager == null) {
//			presentation.setEnabled(false);
//			presentation.setVisible(false);
//			return;
//		}
//		if (activeVcs == null || !(activeVcs instanceof MksVcs)) {
//			presentation.setEnabled(false);
//			presentation.setVisible(false);
//			return;
//		}
        VirtualFile[] files = (VirtualFile[]) dataContext.getData(MksVcs.DATA_CONTEXT_VIRTUAL_FILE_ARRAY);
        if (files == null || files.length == 0) {
            presentation.setEnabled(false);
            presentation.setVisible(true);
            return;
        }
        boolean enabled = true;
        ProjectLevelVcsManager projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project);
        for (VirtualFile file : files) {
            AbstractVcs activeVcs = projectLevelVcsManager.getVcsFor(file);
            if (activeVcs == null || !(activeVcs instanceof MksVcs) || !isEnabled(project, activeVcs, file)) {
                enabled = false;
                break;
            }
        }

        presentation.setEnabled(enabled);
        presentation.setVisible(enabled);
    }

    private void execute(final Project project, VirtualFile[] files, List exceptions, DataContext dataContext) {
        final VirtualFile[] affectedFiles = collectAffectedFiles(project, files);


        perform(project, affectedFiles, dataContext, exceptions);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                for (VirtualFile file : affectedFiles) {
                    file.refresh(false, true);
                    FileStatusManager.getInstance(project).fileStatusChanged(file);
                }
            }

        });

    }

    /**
     * This method allow subclasses to enable batch actions if appropriate
     * Default behavior is to invoke perform on each element
     *
     * @param project
     * @param affectedFiles
     * @param dataContext
     * @param exceptions
     */
    protected void perform(Project project, VirtualFile[] affectedFiles, DataContext dataContext, List exceptions) {
        ProjectLevelVcsManager projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project);
        for (int i = 0; i < affectedFiles.length; i++) {
            final VirtualFile file = affectedFiles[i];
            MksVcs vcs = (MksVcs) projectLevelVcsManager.getVcsFor(file);
            Module module = MksVcs.findModule(project, file);
            try {
                synchronized (vcs) {
                    perform(project, module, vcs, file, dataContext);
                }
            }
            catch (VcsException ex) {
                ex.setVirtualFile(file);
                exceptions.add(ex);
                i++;
                i++;
            }
        }
    }

    protected VirtualFile[] collectAffectedFiles(Project project, VirtualFile[] files) {
        List<VirtualFile> affectedFiles = new ArrayList<VirtualFile>(files.length);
        ProjectLevelVcsManager projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project);
        for (VirtualFile file : files) {
            if (!file.isDirectory()) {
                if (projectLevelVcsManager.getVcsFor(file) instanceof MksVcs) {
                    affectedFiles.add(file);
                }
            } else if (file.isDirectory() && isRecursive()) {
                addChildren(affectedFiles, file);
            }

        }
        return affectedFiles.toArray(new VirtualFile[0]);
    }

    /**
     * recursively adds all the non directory children of file to the files list
     *
     * @param files
     * @param file
     */
    private void addChildren(List<VirtualFile> files, VirtualFile file) {
        VirtualFile[] children = file.getChildren();
        for (VirtualFile child : children) {
            if (!file.isDirectory()) {
                files.add(child);
            } else if (file.isDirectory() && isRecursive()) {
                addChildren(files, child);
            }
        }
    }

    protected abstract String getActionName(AbstractVcs abstractvcs);

    protected abstract boolean isEnabled(Project project, AbstractVcs abstractvcs, VirtualFile virtualfile);

    protected abstract void perform(Project project, Module module, MksVcs mksvcs, VirtualFile virtualfile, DataContext datacontext)
            throws VcsException;


    protected boolean isRecursive() {
        return true;
    }

    protected boolean appliesTo(VirtualFile file) {
        return !file.isDirectory();
    }

    protected TriclopsSiMembers createSiMembers(VirtualFile file, MksVcs vcs) throws VcsException {
        try {
            TriclopsSiSandbox sandbox = MKSHelper.getSandbox(file);
            TriclopsSiMembers members = MKSHelper.createMembers(sandbox);
            members.addMember(new TriclopsSiMember(file.getPresentableUrl()));
            MKSHelper.getMembersStatus(members);
            return members;
        } catch (TriclopsException e) {
            throw new VcsException("Unable to obtain file status");
        }

    }
}
