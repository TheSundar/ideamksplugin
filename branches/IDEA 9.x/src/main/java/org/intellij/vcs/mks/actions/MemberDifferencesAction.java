package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.MemberDifferencesTriclopsCommand;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class MemberDifferencesAction extends SingleTargetAction {

	public MemberDifferencesAction() {
		super(new MemberDifferencesTriclopsCommand());
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		return
				super.isEnabled(project, vcs, virtualFiles)
						&& !virtualFiles[0].isDirectory() &&
						FileStatusManager.getInstance(project).getStatus(virtualFiles[0]) != FileStatus.ADDED;
	}

}
