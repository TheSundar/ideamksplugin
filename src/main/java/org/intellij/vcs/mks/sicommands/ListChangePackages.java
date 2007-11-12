package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thibaut Fagart
 */
public class ListChangePackages extends SiCLICommand {
	public List<MksChangePackage> changePackages;
	@NonNls
	public static final String COMMAND = "viewcps";
	public static final int ID = 0;
	public static final int USER = 1;
	public static final int STATE = 2;
	public static final int SUMMARY = 3;
	public static final String ARGS = "--fields=id,user,state,summary";
	private final String server;

	public ListChangePackages(List<VcsException> errors, EncodingProvider encodingProvider, final MksServerInfo server) {
		super(errors, encodingProvider, COMMAND, (server == null) ? new String[]{ARGS} : new String[]{ARGS, "--hostname", server.host});
		this.server = server.host;
	}

	@Override
	public void execute() {
		ArrayList<MksChangePackage> tempChangePackages = new ArrayList<MksChangePackage>();
		try {
			String command = executeCommand();
			String[] lines = commandOutput.split("\n");
			int start = 0;
			while (shouldIgnore(lines[start])) {
				// skipping connecting/reconnecting lines
				start++;
			}
			for (int i = start, max = lines.length; i < max; i++) {
				String line = lines[i];
				String[] parts = line.split("\t");
				if (parts.length < 4) {
					String errrorMessage = "unexpected command output {" + line + "}, expected 4 parts separated by \\t, while executing " + command;
					LOGGER.error(errrorMessage, "");
					//noinspection ThrowableInstanceNeverThrown
					errors.add(new VcsException(errrorMessage));
				} else {
					tempChangePackages.add(new MksChangePackage(server, parts[ID], parts[USER], parts[STATE], parts[SUMMARY]));
				}
			}
			changePackages = tempChangePackages;
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}
	}

	@Override
	public String toString() {
		return "ListChangePackages[" + server + "]";
	}
}