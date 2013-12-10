package org.intellij.vcs.mks;

import org.jetbrains.annotations.NotNull;

/**
 * @author Thibaut Fagart
 */
public interface MksCLIConfiguration {
	@NotNull
	String getMksSiEncoding(String command);

	@NotNull
	String getDatePattern();

	CommandExecutionListener getCommandExecutionListener();

	/**
	 * true if mks version is 2007.
	 * This is needed because --fields options changes when fetching the locking sandbox 
	 * @return
	 */
	boolean isMks2007();
}
