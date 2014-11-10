package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksVcsException;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SiConnectCommandBatchAPI extends SiAPICommand {

	private final String password;
	private String host;
	private String port;
	private final String username;
	private MksServerInfo server;

	public SiConnectCommandBatchAPI(@NotNull MksCLIConfiguration mksCLIConfiguration,
									@NotNull String host, @NotNull String port, @NotNull String user, @NotNull String password) {
		super(new ArrayList<VcsException>(), "connect", mksCLIConfiguration);
		this.host = host;
		this.port = port;
		this.username = user;
		this.password = password;
	}

	@Override
	protected void handleResponse(Response response) throws APIException {
		this.server = new MksServerInfo(this.username, this.host, this.port);
		// no op, exception is thrown is command failed
	}

	@Override
	protected Command createAPICommand() {

		Command command = new Command(Command.SI);
		command.setCommandName("connect");
		command.addOption(new Option("user", this.username));
		command.addOption(new Option("password", this.password));
		command.addOption(new Option("hostname", this.host));
		command.addOption(new Option("port", this.port));
		command.addOption(new Option("batch"));
		return command;
	}

	public MksServerInfo getServer() {
		return server;
	}
	@Override
	protected void logCommand(StringBuilder buf) {
		final int startPwd = buf.indexOf("--password=");
		final int endPwd = buf.indexOf(" ", startPwd);
		for (int i = startPwd + "--password=".length(); i < endPwd; i++)
			buf.setCharAt(i, '*');
		super.logCommand(buf);
	}
}
