package org.intellij.vcs.mks.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.MksVcsException;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiSandbox;


/**
 * Opens the current sandbox in the mks client
 */
public class ViewSandboxAction extends BasicAction {

    @Override
    protected boolean isRecursive() {
        return false;
    }

    public ViewSandboxAction() {
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
        Map<TriclopsSiSandbox, ArrayList<VirtualFile>> map = vcs.dispatchBySandbox(virtualFiles);
        return map.size() == 1;
    }

    @Override
    protected void perform(@NotNull Project project, MksVcs mksVcs, @NotNull List<VcsException> exceptions, @NotNull VirtualFile[] affectedFiles) {
        Map<TriclopsSiSandbox, ArrayList<VirtualFile>> map = mksVcs.dispatchBySandbox(affectedFiles);
        for (TriclopsSiSandbox sandbox : map.keySet()) {
            try {
                MKSHelper.viewSandbox(sandbox);
            } catch (TriclopsException e) {
                //noinspection ThrowableInstanceNeverThrown
                exceptions.add(new MksVcsException("ViewSandbox:  Unable to view sandbox." + sandbox.getPath(), e));
            }
        }
    }

    @Override
    @NotNull
    protected String getActionName(@NotNull AbstractVcs vcs) {
        return "View Sandbox";
    }
}
