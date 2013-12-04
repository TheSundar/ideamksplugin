package org.intellij.vcs.mks.actions.api;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractSingleTargetAPICommand extends AbstractMultipleTargetAPICommand {
	public void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions,
							   @NotNull VirtualFile[] affectedFiles) throws VcsException {
		if (affectedFiles.length > 1) {
			//noinspection ThrowableInstanceNeverThrown
			exceptions.add(new VcsException("expecting only one target for this action"));
			return;
		}

        super.executeCommand(mksVcs, exceptions, affectedFiles);

	}
}
