package org.intellij.vcs.mks.sicommands.cli;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class ResyncCommand extends SiCLICommand {
	public ResyncCommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration,
						 @NotNull final String sandboxPath, @NotNull String... filters) {
		super(errors, mksCLIConfiguration, "resync", false, "--sandbox=" + sandboxPath, "--recurse", "--gui",
				createFilterArg(filters));
	}

	private static String createFilterArg(String[] filters) {
		StringBuilder sw = new StringBuilder("--filter=");
		if (filters.length == 0) {
			sw.append("file:*");
		} else {
			for (int i = 0, max = filters.length; i < max; i++) {
				sw.append("file:\"").append(filters[i]).append("*\"");
				if (i < max - 1) {
					sw.append(",");
				}
			}
		}
		return sw.toString();
	}

	public void execute() {
		try {
			super.executeCommand();
		} catch (IOException e) {
			errors.add(new VcsException(e));
		}
	}
}
