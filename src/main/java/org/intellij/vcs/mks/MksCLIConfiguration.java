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
}
