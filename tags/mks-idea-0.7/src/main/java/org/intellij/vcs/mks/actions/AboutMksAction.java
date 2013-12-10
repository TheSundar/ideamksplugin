package org.intellij.vcs.mks.actions;

import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.AboutMksTriclopsMksCommand;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class AboutMksAction extends BasicAction {
	public AboutMksAction() {
		super(new AboutMksTriclopsMksCommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs, @NotNull VirtualFile... vFiles) {
		return true;
	}

}
