package org.intellij.vcs.mks.actions.api;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.DispatchBySandboxCommand;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.MksCommand;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAPICommand implements MksCommand {
    /**
     * precondition : ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(mksvcs,
     * vFiles)
     *
     * @param vcs   the vcs for this project
     * @param files a list of files, all should be under mks, but do not to have
     *              to be under the same sandbox
     * @return selected members grouped by sandbox
     * @throws com.intellij.openapi.vcs.VcsException
     *          if anything goes wrong
     */
    @NotNull
    protected Map<MksSandboxInfo, String[]> createMembers(@NotNull MksVcs vcs, @NotNull VirtualFile... files) throws
            VcsException {
        DispatchBySandboxCommand dispatchAction =
                new DispatchBySandboxCommand(vcs, files);
        dispatchAction.execute();

        Map<MksSandboxInfo, ArrayList<VirtualFile>> filesBysandbox = dispatchAction.getFilesBySandbox();
        Map<MksSandboxInfo, String[]> result = new HashMap<MksSandboxInfo, String[]>();
        for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : filesBysandbox.entrySet()) {

            String[] members = new String[entry.getValue().size()];
            result.put(entry.getKey(), members);
            int j = 0;
            for (VirtualFile virtualFile : entry.getValue()) {
                members[j++] = virtualFile.getPresentableUrl();

            }

        }
        return result;
    }

}
