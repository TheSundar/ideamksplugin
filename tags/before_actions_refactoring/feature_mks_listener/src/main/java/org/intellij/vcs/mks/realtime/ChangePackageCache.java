package org.intellij.vcs.mks.realtime;

import org.intellij.vcs.mks.model.MksChangePackage;

/**
 * @author Thibaut Fagart
 */
public interface ChangePackageCache {
	void clear();

	void addChangePackage(MksChangePackage mksChangePackage);
}
