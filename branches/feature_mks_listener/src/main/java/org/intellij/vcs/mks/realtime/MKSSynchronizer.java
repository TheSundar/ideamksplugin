package org.intellij.vcs.mks.realtime;

/**
 * @author Thibaut Fagart
 */
public interface MKSSynchronizer {
	SandboxListListener getSandboxCache();

	ChangePackageCache getChangePackageCache();
}
