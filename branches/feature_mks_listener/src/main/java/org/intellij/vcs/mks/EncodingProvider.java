package org.intellij.vcs.mks;

import org.jetbrains.annotations.NotNull;

/**
 * @author Thibaut Fagart
 */
public interface EncodingProvider {
	@NotNull
	String getMksSiEncoding(String command);
}
