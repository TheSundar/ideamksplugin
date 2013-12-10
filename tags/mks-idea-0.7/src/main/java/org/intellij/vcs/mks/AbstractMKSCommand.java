package org.intellij.vcs.mks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;

/**
 * Command pattern, also feeds the {@link #errors} attribute with errors happening during execution
 *
 * @author Thibaut Fagart
 */
public abstract class AbstractMKSCommand {
	protected final Logger LOGGER = Logger.getInstance(getClass().getName());
	//		Logger.getInstance(getClass().getName());
	public final List<VcsException> errors;
	protected final int previousErrorCount;

	public boolean foundError() {
		return errors.size() > previousErrorCount;
	}

	public AbstractMKSCommand(@NotNull List<VcsException> errors) {
		this.errors = errors;
		previousErrorCount = errors.size();
	}

	public abstract void execute();

	@NotNull
	protected TriclopsSiMembers queryMksMemberStatus(@NotNull ArrayList<VirtualFile> files, @NotNull TriclopsSiSandbox sandbox) throws TriclopsException {
		TriclopsSiMembers members = MKSHelper.createMembers(sandbox);
		for (VirtualFile virtualFile : files) {
			members.addMember(new TriclopsSiMember(virtualFile.getPresentableUrl()));
		}
		MKSHelper.getMembersStatus(members);
		return members;
	}

	@NotNull
	protected HashSet<VirtualFile> getAllMksKnownFoldersInProject(@NotNull VirtualFile sandboxFolder, @NotNull String[] projectMembers) {
		HashSet<VirtualFile> projectFolders = new HashSet<VirtualFile>();
		projectFolders.add(sandboxFolder);
		for (String projectMemberRelativePath : projectMembers) {
			int lastIndexOfSlash = projectMemberRelativePath.lastIndexOf('/');
			if (lastIndexOfSlash >= 0) {
				VirtualFile projectFolder = sandboxFolder.findFileByRelativePath(projectMemberRelativePath.substring(0, lastIndexOfSlash));
				if (projectFolder != null && !projectFolders.contains(projectFolder)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("detected project folder :" + projectFolder);
					}
					// add all parent folders : takes care of parent folders that do not have any file children (only directories)
					do {
						projectFolders.add(projectFolder);
						projectFolder = projectFolder.getParent();
					}
					while (projectFolder != null && !projectFolders.contains(projectFolder) && !projectFolder.equals(sandboxFolder));
				} else if (projectFolder == null) {
					LOGGER.debug("can't find folder for path " + projectMemberRelativePath);
				}
			}
		}
		return projectFolders;
	}
}
