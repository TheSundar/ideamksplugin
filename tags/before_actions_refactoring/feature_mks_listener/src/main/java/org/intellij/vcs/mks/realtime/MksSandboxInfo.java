package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsSiSandbox;

/**
 * @author Thibaut Fagart
 */
public final class MksSandboxInfo implements Comparable<MksSandboxInfo> {
	public final String sandboxPath;
	public final String hostAndPort;
	public final String mksProject;
	public final String devPath;
	final VirtualFile sandboxPjFile;
	TriclopsSiSandbox siSandbox;

	public MksSandboxInfo(final String sandboxPath, final String hostAndPort, String mksProject, String devPath, final VirtualFile sandboxPjFile) {
		this.mksProject = mksProject;
		this.devPath = devPath;
		this.sandboxPjFile = sandboxPjFile;
		this.hostAndPort = hostAndPort;
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
		return "MksSandbox[" + sandboxPath + "," + hostAndPort + "]";
	}
}
