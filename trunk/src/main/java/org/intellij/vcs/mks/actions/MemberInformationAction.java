package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.api.MemberInformationAPICommand;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class MemberInformationAction extends SingleTargetAction {

	public MemberInformationAction() {
		super(new MemberInformationAPICommand());
	}


	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		return
				super.isEnabled(project, vcs, virtualFiles)
						&& !virtualFiles[0].isDirectory();
	}
}
