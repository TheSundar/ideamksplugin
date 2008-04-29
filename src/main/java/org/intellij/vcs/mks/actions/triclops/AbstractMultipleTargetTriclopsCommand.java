package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.MksVcsException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;

public abstract class AbstractMultipleTargetTriclopsCommand extends AbstractTriclopsCommand {
	public void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions,
							   @NotNull VirtualFile[] affectedFiles) throws VcsException {
		TriclopsSiMembers[] members = createSiMembers(mksVcs, affectedFiles);
		for (TriclopsSiMembers siMembers : members) {
			try {
				// todo if active change list is an mks one, use it as the change package ?
				perform(siMembers);
			} catch (TriclopsException e) {
				if (!MksVcs.isLastCommandCancelled()) {
					@Nls final String message = "{0} Error: {1}";
					//noinspection ThrowableInstanceNeverThrown
					exceptions.add(new MksVcsException(
							MessageFormat.format(message, getActionName(mksVcs), MksVcs.getMksErrorMessage()), e));
				}
			}
		}
	}

	protected abstract void perform(@NotNull TriclopsSiMembers siMembers) throws TriclopsException;

}
