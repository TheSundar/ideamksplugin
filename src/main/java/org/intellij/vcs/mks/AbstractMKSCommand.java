package org.intellij.vcs.mks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

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
	protected final MksCLIConfiguration mksCLIConfiguration;
	protected String command;

	public boolean foundError() {
		return errors.size() > previousErrorCount;
	}

	public AbstractMKSCommand(@NotNull List<VcsException> errors, @NotNull String command,
							  @NotNull MksCLIConfiguration mksCLIConfiguration) {
		this.errors = errors;
		previousErrorCount = errors.size();
		this.command = command;
		this.mksCLIConfiguration = mksCLIConfiguration;
	}

	public abstract void execute();

	@NotNull
	protected HashSet<VirtualFile> getAllMksKnownFoldersInProject(@NotNull VirtualFile sandboxFolder,
																  @NotNull String[] projectMembers) {
		HashSet<VirtualFile> projectFolders = new HashSet<VirtualFile>();
		projectFolders.add(sandboxFolder);
		for (String projectMemberRelativePath : projectMembers) {
			int lastIndexOfSlash = projectMemberRelativePath.lastIndexOf('/');
			if (lastIndexOfSlash >= 0) {
				VirtualFile projectFolder =
						sandboxFolder.findFileByRelativePath(projectMemberRelativePath.substring(0, lastIndexOfSlash));
				if (projectFolder != null && !projectFolders.contains(projectFolder)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("detected project folder :" + projectFolder);
					}
					// add all parent folders : takes care of parent folders that do not have any file children (only directories)
					do {
						projectFolders.add(projectFolder);
						projectFolder = projectFolder.getParent();
					}
					while (projectFolder != null && !projectFolders.contains(projectFolder) &&
							!projectFolder.equals(sandboxFolder));
				} else if (projectFolder == null) {
					LOGGER.debug("can't find folder for path " + projectMemberRelativePath);
				}
			}
		}
		return projectFolders;
	}

	protected void fireCommandCompleted(long start) {
		CommandExecutionListener listener = getCommandExecutionListener();
		listener.executionCompleted(command, System.currentTimeMillis() - start);
		LOGGER.debug(toString() + " finished in " + (System.currentTimeMillis() - start + " ms"));
	}

	private CommandExecutionListener getCommandExecutionListener() {
		return mksCLIConfiguration.getCommandExecutionListener();
	}
}
