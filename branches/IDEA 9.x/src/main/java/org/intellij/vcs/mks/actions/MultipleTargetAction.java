package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;


public abstract class MultipleTargetAction extends BasicAction {

	protected MultipleTargetAction(MksCommand command) {
		super(command);
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
		return true;
	}
}
