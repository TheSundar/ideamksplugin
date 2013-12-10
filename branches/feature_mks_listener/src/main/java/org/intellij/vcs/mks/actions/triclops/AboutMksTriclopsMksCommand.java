package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksVcs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AboutMksTriclopsMksCommand extends AbstractTriclopsCommand {

	public void executeCommand(@NotNull final MksVcs mksVcs, @NotNull List<VcsException> exceptions, @NotNull VirtualFile[] affectedFiles) throws VcsException {
		ApplicationManager.getApplication().runReadAction(new Runnable() {
			public void run() {
				try {
					MKSHelper.aboutBox();
				} catch (TriclopsException e) {
					ArrayList<VcsException> errors = new ArrayList<VcsException>();
					//noinspection ThrowableInstanceNeverThrown
					errors.add(new VcsException(e));
					MksVcs.getInstance(mksVcs.getProject()).showErrors(errors, "About MKS");
				}
			}
		});
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.about.mks");
	}
}
