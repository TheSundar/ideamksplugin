package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.actions.cli.ResyncCLICommand;
import org.jetbrains.annotations.NotNull;

// Referenced classes of package org.intellij.vcs.mks.actions:
//            BasicAction

public class ResynchronizeMembersAction extends MultipleTargetAction {

	public ResynchronizeMembersAction() {
		super(new ResyncCLICommand());
	}

	/**
	 * just return the selected files
	 *
	 * @param project
	 * @param files   the files target of the action
	 * @return the files/dirs that should be resynchronized
	 */
	@NotNull
	protected VirtualFile[] collectAffectedFiles(@NotNull Project project, @NotNull VirtualFile[] files) {
		return files;
	}
}
