package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.MksVcsException;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class BasicAction extends AnAction {
	protected static final String ACTION_CANCELLED_MSG = "The command was cancelled.";
	protected final MksCommand command;

	public BasicAction(MksCommand command) {
		this.command = command;
	}

	protected final void perform(@NotNull Project project, MksVcs mksVcs, @NotNull List<VcsException> exceptions,
								 @NotNull VirtualFile[] affectedFiles) {
		try {
			command.executeCommand(mksVcs, exceptions, affectedFiles);
		} catch (VcsException e) {
			//noinspection ThrowableInstanceNeverThrown
			exceptions.add(new MksVcsException(/*"Unable to obtain file status"*/ e.getMessage(), e));
		}

		WindowManager.getInstance().getStatusBar(project)
				.setInfo(MessageFormat.format(MksBundle.message("action.complete"), getActionName(mksVcs)));
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent event) {

        final Project project = getProject(event);
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			public void run() {
				FileDocumentManager.getInstance().saveAllDocuments();
			}
		});
        final VirtualFile[] vFiles = getVirtualFiles(event);

		final MksVcs mksvcs = MksVcs.getInstance(project);
		if (!ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(mksvcs, vFiles)) {
			return;
		}

		final List<VcsException> exceptions = new ArrayList<VcsException>();
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			public void run() {
				final VirtualFile[] affectedFiles = collectAffectedFiles(project, vFiles);
				//noinspection unchecked
				perform(project, mksvcs, exceptions, affectedFiles);
				for (VirtualFile file : affectedFiles) {
					file.refresh(false, true);
					FileStatusManager.getInstance(project).fileStatusChanged(file);
				}
			}
		});
		mksvcs.showErrors(exceptions, getActionName(mksvcs));


	}

    private VirtualFile[] getVirtualFiles(AnActionEvent event) {
        return PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
    }

    private Project getProject(AnActionEvent event) {
        return PlatformDataKeys.PROJECT.getData(event.getDataContext());
    }

    /**
	 * given a list of action-target files, returns ALL the files that should be
	 * subject to the action Does not keep directories, but recursively adds
	 * directory contents
	 *
	 * @param project the project subject of the action
	 * @param files   the root selection
	 * @return the complete set of files this action should apply to
	 */
	@NotNull
	protected VirtualFile[] collectAffectedFiles(@NotNull Project project, @NotNull VirtualFile[] files) {
		List<VirtualFile> affectedFiles = new ArrayList<VirtualFile>(files.length);
		ProjectLevelVcsManager projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project);
		for (VirtualFile file : files) {
			if (!file.isDirectory() && projectLevelVcsManager.getVcsFor(file) instanceof MksVcs) {
				affectedFiles.add(file);
			} else if (file.isDirectory() && isRecursive()) {
				addChildren(project, affectedFiles, file);
			}

		}
		return affectedFiles.toArray(new VirtualFile[affectedFiles.size()]);
	}

	/**
	 * recursively adds all the children of file to the files list, for which
	 * this action makes sense ({@link #appliesTo(com.intellij.openapi.project.Project,com.intellij.openapi.vfs.VirtualFile)}
	 * returns true)
	 *
	 * @param project the project subject of the action
	 * @param files   result list
	 * @param file	the file whose children should be added to the result list
	 *                (recursively)
	 */
	private void addChildren(@NotNull Project project, @NotNull List<VirtualFile> files, @NotNull VirtualFile file) {
		VirtualFile[] children = file.getChildren();
		for (VirtualFile child : children) {
			if (!child.isDirectory() && appliesTo(project, child)) {
				files.add(child);
			} else if (child.isDirectory() && isRecursive()) {
				addChildren(project, files, child);
			}
		}
	}

	@NotNull
	protected final String getActionName(@NotNull AbstractVcs abstractvcs) {
		return command.getActionName(abstractvcs);
	}

	protected boolean isRecursive() {
		return true;
	}

	protected boolean appliesTo(@NotNull Project project, @NotNull VirtualFile file) {
		return !file.isDirectory();
	}

	/**
	 * disable the action if the event does not apply on MksVcs enabled
	 * resources Hide it if the event does not have a project, or if no
	 * VirtualFiel are targeted
	 *
	 * @param e the update event
	 */
	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		Presentation presentation = e.getPresentation();
		DataContext dataContext = e.getDataContext();
		Project project = getProject(e);
		if (project == null) {
			presentation.setEnabled(false);
			presentation.setVisible(false);
			return;
		}

		VirtualFile[] vFiles = getVirtualFiles(e);
		if (vFiles == null || vFiles.length == 0) {
			presentation.setEnabled(false);
			presentation.setVisible(true);
			return;
		}
		MksVcs mksvcs = MksVcs.getInstance(project);
		boolean enabled = ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(mksvcs, vFiles)
				&& isEnabled(project, mksvcs, vFiles);
		// only enable action if all the targets are under the vcs and the action suports all of them

		presentation.setEnabled(enabled);
		presentation.setVisible(enabled);
	}

	protected abstract boolean isEnabled(@NotNull Project project, @NotNull MksVcs mksvcs,
										 @NotNull VirtualFile... vFiles);

}
