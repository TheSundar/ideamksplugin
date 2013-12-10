package org.intellij.vcs.mks.actions;

import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.MemberHistoryTriclopsCommand;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;


public class MemberHistoryAction extends SingleTargetAction {

	public MemberHistoryAction() {
		super(new MemberHistoryTriclopsCommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		return
				super.isEnabled(project, vcs, virtualFiles)
						&& !virtualFiles[0].isDirectory() && virtualFiles[0].isValid();
	}
}
