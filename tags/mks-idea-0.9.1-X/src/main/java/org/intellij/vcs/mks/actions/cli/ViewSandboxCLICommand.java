package org.intellij.vcs.mks.actions.cli;

import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.MksCommand;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.sicommands.SiCLICommand;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewSandboxCLICommand implements MksCommand {
	static class ViewSandboxCommand extends SiCLICommand {
		private final String sandboxPath;

		ViewSandboxCommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration,
						   MksSandboxInfo sandbox) {
			super(errors, mksCLIConfiguration, "viewsandbox", "--gui", "--sandbox=" + sandbox.sandboxPath);
			sandboxPath = sandbox.sandboxPath;
		}

		public void execute() {
			try {
				executeCommand();
			} catch (IOException e) {
				LOGGER.error(
						MessageFormat.format(MksBundle.message("error.opening.sandbox.in.mks.client"), sandboxPath), e);
			}
		}
	}

	public void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions,
							   @NotNull VirtualFile[] affectedFiles) throws VcsException {
		Map<MksSandboxInfo, ArrayList<VirtualFile>> map = mksVcs.dispatchBySandbox(affectedFiles);

		for (MksSandboxInfo sandbox : map.keySet()) {
			new ViewSandboxCommand(exceptions, mksVcs, sandbox).execute();
		}
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.view.sandbox");
	}
}
