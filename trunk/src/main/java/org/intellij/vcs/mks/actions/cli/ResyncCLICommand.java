package org.intellij.vcs.mks.actions.cli;

import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.MksCommand;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.sicommands.cli.ResyncCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResyncCLICommand implements MksCommand {
	public void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions,
							   @NotNull VirtualFile[] affectedFiles) throws VcsException {
		final Map<MksSandboxInfo, ArrayList<VirtualFile>> filesBySandbox =
				mksVcs.dispatchBySandbox(affectedFiles, false);
		for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : filesBySandbox
				.entrySet()) {
			final ArrayList<VirtualFile> files = entry.getValue();
			String[] relativePaths = new String[files.size()];
			int i = 0;
			for (VirtualFile file : files) {
				relativePaths[i++] = entry.getKey().getRelativePath(file, '/');
			}

			final ResyncCommand resyncCommand =
					new ResyncCommand(exceptions, mksVcs, entry.getKey().sandboxPath, relativePaths);
			resyncCommand.execute();
		}
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.resync");
	}
}
