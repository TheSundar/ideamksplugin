package org.intellij.vcs.mks.actions;

import java.util.List;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.MksVcsException;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;


public abstract class MultipleTargetAction extends BasicAction {
    @Override
    protected void perform(@NotNull Project project, MksVcs mksVcs, @NotNull List<VcsException> exceptions, @NotNull VirtualFile[] affectedFiles) {
        try {
            TriclopsSiMembers[] members = createSiMembers(mksVcs, affectedFiles);
            for (TriclopsSiMembers siMembers : members) {
                try {
                    // todo if active change list is an mks one, use it as the change package ?
                    perform(siMembers);
                } catch (TriclopsException e) {
                    if (!MksVcs.isLastCommandCancelled()) {
                        //noinspection ThrowableInstanceNeverThrown
                        exceptions.add(new MksVcsException(getActionName(mksVcs) +
                            " Error: " + MksVcs.getMksErrorMessage(), e));
                    }
                }
            }
        } catch (VcsException e) {
            //noinspection ThrowableInstanceNeverThrown
            exceptions.add(new MksVcsException("Unable to obtain file status", e));
        }

        WindowManager.getInstance().getStatusBar(project).setInfo(getActionName(mksVcs) + " complete.");
    }

    protected abstract void perform(TriclopsSiMembers siMembers) throws TriclopsException;

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
        return true;
    }
}
