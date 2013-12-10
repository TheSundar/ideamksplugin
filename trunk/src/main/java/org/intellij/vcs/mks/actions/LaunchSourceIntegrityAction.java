package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.mks.api.response.APIException;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LaunchSourceIntegrityAction extends BasicAction {

    public LaunchSourceIntegrityAction() {
        super( new MksCommand() {
            @Override
            public void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions, @NotNull VirtualFile[] affectedFiles) throws VcsException {
                try {
                    new MKSAPIHelper.SICommands(MKSAPIHelper.getInstance().getSession()).launchMKSGUI();
                } catch (APIException e) {
                    exceptions.add(new VcsException(e));
                }
            }

            @NotNull
            @Override
            public String getActionName(@NotNull AbstractVcs vcs) {
                return "Launch Source Integrity";
            }
        });
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
        return true;
    }
}
