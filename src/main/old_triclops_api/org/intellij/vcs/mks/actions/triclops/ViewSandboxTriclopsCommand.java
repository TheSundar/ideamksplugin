package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import org.intellij.vcs.mks.MKSHelper;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.actions.MksCommand;
import org.intellij.vcs.mks.realtime.MksNativeSandboxInfo;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Deprecated
public class ViewSandboxTriclopsCommand implements MksCommand {
	private final Logger LOGGER = Logger.getInstance(getClass().getName());

	public void executeCommand(@NotNull MksVcs mksVcs, @NotNull List<VcsException> exceptions,
							   @NotNull VirtualFile[] affectedFiles) throws VcsException {
		Map<MksSandboxInfo, ArrayList<VirtualFile>> map = mksVcs.dispatchBySandbox(affectedFiles);
		for (MksSandboxInfo sandbox : map.keySet()) {
            if (sandbox instanceof MksNativeSandboxInfo) {
                try {
                    MKSHelper.viewSandbox(((MksNativeSandboxInfo) sandbox).getSiSandbox());
                } catch (TriclopsException e) {
                    //noinspection ThrowableInstanceNeverThrown
                    LOGGER.error(MessageFormat.format(MksBundle.message("error.opening.sandbox.in.mks.client"),
                            sandbox.sandboxPath), e);
                }
            } else {
                throw new UnsupportedOperationException("native");
            }
        }
	}

	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.view.sandbox");
	}
}
