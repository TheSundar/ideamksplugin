package org.intellij.vcs.mks.realtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SandboxListListener {
	void clear();

	/**
	 * @param sandboxPath	   absolute path the .pj file on the local filesystem
	 * @param serverHostAndPort host:port of the server that hosts the project
	 * @param mksProject		the mks project path this sandbox is associated with
	 * @param devPath		   null if the sandbox is on the trunk
	 * @param isSubSandbox	  true if the sandbox is not a top level sandbox
	 */
	void addSandboxPath(@NotNull String sandboxPath, @NotNull String serverHostAndPort, @NotNull String mksProject,
						@Nullable String devPath, boolean isSubSandbox);
}
