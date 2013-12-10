package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

/**
 * An action only applicable on a single file
 */
public abstract class SingleTargetAction extends BasicAction {

	protected SingleTargetAction(MksCommand command) {
		super(command);
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
		return vFiles.length == 1;
	}

}
