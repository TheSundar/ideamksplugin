package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MKSHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thibaut Fagart
 */
public final class MksNativeSandboxInfo extends MksSandboxInfo {
    private TriclopsSiSandbox siSandbox;

	@Deprecated
	public synchronized TriclopsSiSandbox getSiSandbox() {
		if (siSandbox == null) {
			try {
				siSandbox = MKSHelper.createSandbox(sandboxPath);
			} catch (TriclopsException e) {
				LOGGER.error("error fetching MKS native sandbox for " + sandboxPath);
			}
		}
		return siSandbox;
	}

	/**
	 * @param sandboxPath
	 * @param hostAndPort
	 * @param mksProject
	 * @param devPath	   null if the sandbox is on the trunk
	 * @param sandboxPjFile null if IDEA has no VirtualFile for the sandbox file
	 * @param isSubSandbox
	 */
	public MksNativeSandboxInfo(@NotNull final String sandboxPath, @NotNull final String hostAndPort,
                                @NotNull String mksProject, @Nullable String devPath,
                                @Nullable final VirtualFile sandboxPjFile, boolean isSubSandbox) {
        super(sandboxPjFile, hostAndPort, isSubSandbox, mksProject, sandboxPath, devPath);
    }

}
