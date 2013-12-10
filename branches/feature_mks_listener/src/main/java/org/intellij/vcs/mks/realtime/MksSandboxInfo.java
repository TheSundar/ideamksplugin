package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MKSHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thibaut Fagart
 */
public final class MksSandboxInfo implements Comparable<MksSandboxInfo> {
	private final Logger LOGGER = Logger.getInstance(getClass().getName());
	public final String sandboxPath;
	public final String hostAndPort;
	public final String mksProject;
	public final String devPath;
	public final boolean isSubSandbox;
	final VirtualFile sandboxPjFile;
	int retries = 0;
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
	public MksSandboxInfo(@NotNull final String sandboxPath, @NotNull final String hostAndPort, @NotNull String mksProject, @Nullable String devPath, @Nullable final VirtualFile sandboxPjFile, boolean isSubSandbox) {
		this.mksProject = mksProject;
		this.devPath = devPath;
		this.sandboxPjFile = sandboxPjFile;
		this.isSubSandbox = isSubSandbox;
		this.hostAndPort = hostAndPort.toLowerCase();
		this.sandboxPath = sandboxPath;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MksSandboxInfo that = (MksSandboxInfo) o;

		return !(devPath != null ? !devPath.equals(that.devPath) : that.devPath != null)
				&& hostAndPort.equals(that.hostAndPort)
				&& mksProject.equals(that.mksProject)
				&& sandboxPath.equals(that.sandboxPath);
	}

	@Override
	public int hashCode() {
		int result;
		result = sandboxPath.hashCode();
		result = 31 * result + hostAndPort.hashCode();
		result = 31 * result + mksProject.hashCode();
		result = 31 * result + (devPath != null ? devPath.hashCode() : 0);
		return result;
	}

	public int compareTo(final MksSandboxInfo sandboxInfo) {
		return sandboxPath.compareTo((sandboxInfo == null) ? null : sandboxInfo.sandboxPath);
	}

	@Override
	public String toString() {
		return "MksSandbox[" + sandboxPath + "," + hostAndPort + (isSubSandbox ? ",subsandbox" : "") + "]";
	}
}
