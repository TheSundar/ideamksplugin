package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.Command;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksVcsException;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SiConnectCommandAPI extends SiAPICommand {

	private final String password;
	private String host;
	private String port;
	private final String username;
	private MksServerInfo server;

	public SiConnectCommandAPI(@NotNull MksCLIConfiguration mksCLIConfiguration,
							   @NotNull String host, @NotNull String port, @NotNull String user, @NotNull String password) {
		super(new ArrayList<VcsException>(), "connect", mksCLIConfiguration);
		this.host = host;
		this.port = port;
		this.username = user;
		this.password = password;
	}

	@Override
	public void execute() {
		long start = System.currentTimeMillis();
		int iPort = Integer.parseInt(port);
		try {
			StringBuilder buf = new StringBuilder("si connect --host=");
			buf.append(host).append(" --port=").append(port).append(" --username=").append(username).append(" --password=***");
			logCommand(buf);
			getAPIHelper().getSICommands().siConnect(host, iPort, username, password);
			server = new MksServerInfo(username, host, port);
		} catch (APIException e) {
			errors.add(new MksVcsException("unable to connect to " + host, e));
		} finally {
			fireCommandCompleted(start);
		}
	}

	@Override
	protected void handleResponse(Response response) throws APIException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Command createAPICommand() {
		throw new UnsupportedOperationException();
	}

	public MksServerInfo getServer() {
		return server;
	}
}
