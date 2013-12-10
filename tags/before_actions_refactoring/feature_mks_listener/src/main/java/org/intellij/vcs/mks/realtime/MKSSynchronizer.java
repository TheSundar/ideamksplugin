package org.intellij.vcs.mks.realtime;

/**
 * @author Thibaut Fagart
 */
public interface MKSSynchronizer {
	SandboxCache getSandboxCache();
	ChangePackageCache getChangePackageCache();
}
