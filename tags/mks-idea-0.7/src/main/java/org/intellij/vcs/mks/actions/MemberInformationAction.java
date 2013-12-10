package org.intellij.vcs.mks.actions;

import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.triclops.MemberInformationTriclopsCommand;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class MemberInformationAction extends SingleTargetAction {

	public MemberInformationAction() {
		super(new MemberInformationTriclopsCommand());
	}


	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		return
				super.isEnabled(project, vcs, virtualFiles)
						&& !virtualFiles[0].isDirectory();
	}
}
