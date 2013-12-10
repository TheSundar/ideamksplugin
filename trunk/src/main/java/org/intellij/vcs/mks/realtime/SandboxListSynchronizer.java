package org.intellij.vcs.mks.realtime;

import org.jetbrains.annotations.NotNull;

public interface SandboxListSynchronizer extends LongRunningTask {
	void addListener(@NotNull SandboxListListener sandboxListListener);

	void removeListener(@NotNull SandboxListListener sandboxListListener);
}
