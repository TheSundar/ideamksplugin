package org.intellij.vcs.mks.actions;

import java.util.ArrayList;
import java.util.Map;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.ViewSandboxTriclopsCommand;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;


/**
 * Opens the current sandbox in the mks client
 */
public class ViewSandboxAction extends BasicAction {

	@Override
	protected boolean isRecursive() {
		return false;
	}

	public ViewSandboxAction() {
		super(new ViewSandboxTriclopsCommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		Map<MksSandboxInfo, ArrayList<VirtualFile>> map = vcs.dispatchBySandbox(virtualFiles);
		return map.size() == 1;
	}
}
