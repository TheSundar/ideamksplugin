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

/**
 * An action only applicable on a single file
 */
public abstract class SingleTargetAction extends BasicAction {
    @Override
    protected void perform(@NotNull Project project, MksVcs mksVcs, @NotNull List<VcsException> exceptions, @NotNull VirtualFile[] affectedFiles) {
        try {
            if (affectedFiles.length > 1) {
                //noinspection ThrowableInstanceNeverThrown
                exceptions.add(new VcsException("expecting only one target for this action"));
                return;
            }
            TriclopsSiMembers[] members = createSiMembers(mksVcs, affectedFiles);
            for (TriclopsSiMembers siMembers : members) {
                try {
                    // todo if active change list is an mks one, use it as the change package ?
                    perform(siMembers);
                } catch (TriclopsException e) {
                    if (MksVcs.isLastCommandCancelled()) {
                        //noinspection ThrowableInstanceNeverThrown
                        exceptions.add(new MksVcsException(getActionName(mksVcs) +
                            " Error: " + MksVcs.getMksErrorMessage(), e));
                    }
                }
            }
        } catch (VcsException e) {
            exceptions.add(e);
        }

        WindowManager.getInstance().getStatusBar(project).setInfo(getActionName(mksVcs) + " complete.");
    }

    /**
     * @param siMembers will only contain 1 member
     * @throws mks.integrations.common.TriclopsException
     *          propagated from MKS api
     */
    protected abstract void perform(@NotNull TriclopsSiMembers siMembers) throws TriclopsException;

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
        return vFiles.length == 1;
    }

}
