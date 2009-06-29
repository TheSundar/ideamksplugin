/*
 * COPYRIGHT. HSBC HOLDINGS PLC 2008. ALL RIGHTS RESERVED.
 *
 * This software is only to be used for the purpose for which it has been
 * provided. No part of it is to be reproduced, disassembled, transmitted,
 * stored in a retrieval system nor translated in any human or computer
 * language in any way or for any other purposes whatsoever without the
 * prior written consent of HSBC Holdings plc.
 */
package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class SiAboutCommand extends SiCLICommand {
	private boolean isMks2007 = false;

	public SiAboutCommand(@NotNull List<VcsException> errors,
						  @NotNull MksCLIConfiguration mksCLIConfiguration) {
		super(errors, mksCLIConfiguration, "about", true);
	}

	@Override
	public void execute() {
		try {
			executeCommand();
			String[] lines = commandOutput.split("\n");
			int start = 0;
			while (shouldIgnore(lines[start])) {
				// skipping connecting/reconnecting lines
				start++;
			}
			// only top line seems to be interesting, we're not interested in the history
			String line = lines[start];
			if (line.trim().length() == 0) {
				return;
			}

			isMks2007 = (line.contains("2007"));
			LOGGER.info("mks 2007 ? " + isMks2007);
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}
	}

	public boolean isMks2007() {
		return isMks2007;
	}
}
