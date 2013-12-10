package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class MemberDifferencesAction extends SingleTargetAction {

	public MemberDifferencesAction() {
	}

	@Override
	protected void perform(@NotNull TriclopsSiMembers siMembers) throws TriclopsException {
		MKSHelper.openMemberDiffView(siMembers, 0);
	}

	@Override
	@NotNull
	protected String getActionName(@NotNull AbstractVcs vcs) {
		return "Differences";
	}

	@Override
	protected boolean isEnabled(@NotNull Project project, @NotNull MksVcs vcs, @NotNull VirtualFile... virtualFiles) {
		return
			super.isEnabled(project, vcs, virtualFiles)
				&& !virtualFiles[0].isDirectory() && FileStatusManager.getInstance(project).getStatus(virtualFiles[0]) != FileStatus.ADDED;
	}

}
