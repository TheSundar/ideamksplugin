package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.MksVcsException;
import org.intellij.vcs.mks.actions.MksCommand;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewSandboxTriclopsCommand implements MksCommand {
	public void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions, @NotNull VirtualFile[] affectedFiles) throws VcsException {
		Map<MksSandboxInfo, ArrayList<VirtualFile>> map = mksVcs.dispatchBySandbox(affectedFiles);
		for (MksSandboxInfo sandbox : map.keySet()) {
			try {
				MKSHelper.viewSandbox(sandbox.getSiSandbox());
			} catch (TriclopsException e) {
				//noinspection ThrowableInstanceNeverThrown
				exceptions.add(new MksVcsException("ViewSandbox:  Unable to view sandbox." + sandbox.sandboxPath, e));
			}
		}
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return "View Sandbox";
	}
}
