package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class MemberInformationAction extends SingleTargetAction {

	public MemberInformationAction() {
	}

	@Override
	protected void perform(@NotNull TriclopsSiMembers members) throws TriclopsException {
		MKSHelper.openMemberInformationView(members, 0);
	}

	@Override
	@NotNull
	protected String getActionName(@NotNull AbstractVcs vcs) {
		return "Member Information";
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		return
			super.isEnabled(project, vcs, virtualFiles)
				&& !virtualFiles[0].isDirectory();
	}
}
