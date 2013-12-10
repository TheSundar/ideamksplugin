package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewGlobalChangesAction extends BasicAction {
    private static final Logger LOGGER = Logger.getInstance(ViewGlobalChangesAction.class.getName());

    protected String getActionName(AbstractVcs abstractvcs) {
        return "View Global Changes";
    }

    public void actionPerformed(AnActionEvent event) {
        super.actionPerformed(event);
    }

    protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile virtualfile) {
        FilePath filePathOn = PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(virtualfile);
        return vcs.fileIsUnderVcs(filePathOn);
    }

    protected void perform(Project project, Module module, MksVcs mksvcs, VirtualFile file, DataContext datacontext) throws VcsException {
        mksvcs.debug("ViewGlobalChangesAction " + file);
        ArrayList<VirtualFile> queriedFiles = new ArrayList<VirtualFile>();
        TriclopsSiSandbox sandbox = MKSHelper.getSandbox(file);

        long deb = System.currentTimeMillis();
        if (file.isDirectory()) {
            addChildrenRecursive(project, module, file, queriedFiles);
        } else {
            queriedFiles.add(file);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("recursively added files  : " + (System.currentTimeMillis() - deb) + " ms");
        }
        deb = System.currentTimeMillis();
        Map<VirtualFile, FileStatus> statuses = ApplicationManager.getApplication().runReadAction(new MksVcs.CalcStatusComputable(mksvcs, queriedFiles));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("fetched statuses : " + (System.currentTimeMillis() - deb) + " ms");
        }
        mksvcs.setChanges(statuses);
//		for (VirtualFile f : queriedFiles) {
//			FileStatus fileStatus = statuses.get(f);
//			if (fileStatus != FileStatus.NOT_CHANGED) {
//				mksvcs.debug("status different for " + f + " [" + fileStatus + "]");
//			}
//		}

    }

    private void addChildrenRecursive(Project project, Module module, VirtualFile virtualfile, List<VirtualFile> queriedFiles) {
        for (VirtualFile child : virtualfile.getChildren()) {
            if (child.isDirectory()) {
                addChildrenRecursive(project, module, child, queriedFiles);
            } else if (!isIgnored(project, module, child)) {
//				siMembers.addMember(new TriclopsSiMember(child.getPresentableUrl()));
                queriedFiles.add(child);
            } else {
                LOGGER.debug("ignored " + child.getPath());
            }
        }
    }

    /**
     * @param project
     * @param module
     * @param child
     * @return
     */
    private boolean isIgnored(Project project, Module module, VirtualFile child) {
        if (isInExcludedRoot(project, module, child)) return true;

        boolean b = "project.pj".equals(child.getName());
        if (b) {
            LOGGER.debug("ignoring " + child.getPath());
        }
        return b;
    }

    private boolean isInExcludedRoot(Project project, Module module, VirtualFile child) {
        if (module == null) {
            module = MksVcs.findModule(project, child);
            if (module == null) return true;
        }
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] excludeRoots = moduleRootManager.getExcludeRoots();
        for (VirtualFile excludeRoot : excludeRoots) {
            if (child.getPath().startsWith(excludeRoot.getPath())) {
//                System.out.println("" + child.getPath() + " is under excluded root " + excludeRoot.getPath());
                return true;
            }
        }
        return false;
    }

    protected VirtualFile[] collectAffectedFiles(Project project, VirtualFile[] files) {
        return files;
    }

    protected boolean isRecursive() {
        return false;
    }
}
