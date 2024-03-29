package org.intellij.vcs.mks.actions;

import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MksCommand {
	void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions,
						@NotNull VirtualFile[] affectedFiles) throws VcsException;

	@NotNull
	String getActionName(@NotNull AbstractVcs vcs);
}
