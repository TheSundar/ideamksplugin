package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.api.MemberHistoryAPICommand;
import org.jetbrains.annotations.NotNull;


public class MemberHistoryAction extends SingleTargetAction {

	public MemberHistoryAction() {
		super(new MemberHistoryAPICommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		return
				super.isEnabled(project, vcs, virtualFiles)
						&& !virtualFiles[0].isDirectory() && virtualFiles[0].isValid();
	}
}
