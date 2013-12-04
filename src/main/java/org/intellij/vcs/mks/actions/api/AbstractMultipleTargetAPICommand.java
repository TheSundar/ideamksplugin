package org.intellij.vcs.mks.actions.api;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.mks.api.response.APIException;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public abstract class AbstractMultipleTargetAPICommand extends AbstractAPICommand {
	public void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions,
							   @NotNull VirtualFile[] affectedFiles) throws VcsException {
        Map<MksSandboxInfo, String[]> members = createMembers(mksVcs, affectedFiles);
        for (Map.Entry<MksSandboxInfo, String[]> entry : members.entrySet()) {
            try {
                perform(entry.getKey(), entry.getValue());
            } catch (APIException e) {
                    exceptions.add(new VcsException(e));

            }
        }
	}

	protected abstract void perform(@NotNull MksSandboxInfo sandbox, String[] members) throws APIException;

}
