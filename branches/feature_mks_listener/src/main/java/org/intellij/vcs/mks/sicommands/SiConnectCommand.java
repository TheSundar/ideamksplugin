package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Thibaut Fagart
 */
public class SiConnectCommand extends SiCLICommand {
	private final String host;
	private final String port;
	private final String user;
	private MksServerInfo server;
	public static final String COMMAND = "connect";

	public SiConnectCommand(@NotNull EncodingProvider encodingProvider, @NotNull String host, @NotNull String port,
							@NotNull String user, @NotNull String password) {
		super(new ArrayList<VcsException>(), encodingProvider, COMMAND, "--hostname=" + host, "--port=" + port, "--user=" + user, "--password=" + password);
		this.host = host;
		this.port = port;
		this.user = user;
	}

	public void execute() {
		try {
			super.executeCommand();
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}
		if (exitValue == 0) {
			server = new MksServerInfo(user, host, port);
		}
	}

	public MksServerInfo getServer() {
		return server;
	}
}
