package org.intellij.vcs.mks;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.wm.WindowManager;
import com.mks.api.*;
import com.mks.api.response.APIException;
import com.mks.api.response.InvalidCommandSelectionException;
import com.mks.api.response.Response;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.sicommands.api.ListServersAPI;
import org.intellij.vcs.mks.sicommands.api.SiConnectCommandAPI;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MKSAPIHelper implements ApplicationComponent {
    protected final Logger LOGGER = Logger.getInstance(getClass().getName());
    private Session session;
    private SICommands siCommands;
    private static IntegrationPoint ip;

    public Session getSession() {
        return session;
    }

    public void initComponent() {
        try {
            IntegrationPointFactory ipf = IntegrationPointFactory.getInstance();

            ip = ipf.createLocalIntegrationPoint();
            ip.setAutoStartIntegrityClient(true);
            String hostname = ip.getHostname();
            int port = ip.getPort();
            LOGGER.warn("connecting to IntegrationPoint "+hostname+":"+port+"=[secure="+ip.isSecure()+"]");
            Session session1 = ip.getCommonSession();
            session1.setAutoReconnect(true);
            session = session1;
        } catch (APIException e) {
            LOGGER.error("error initializing MKS", e);
        }
    }

    public void disposeComponent() {
        try {
            session.release(true);
        } catch (Exception e) {
            LOGGER.error("error releasing session", e);
            e.printStackTrace();
        }
        try {
            ip.release();
        } catch (Exception e) {
            LOGGER.error("error releasing MKS", e);
        }
    }

    @NotNull
    public String getComponentName() {
        return "MKSAPIHelper";
    }

    public static MKSAPIHelper getInstance() {
        return ApplicationManager.getApplication().getComponent(MKSAPIHelper.class);
    }

    public synchronized SICommands getSICommands() throws APIException {
        if (null == siCommands) {
            siCommands = new SICommands(session);
        }
        return siCommands;
    }

	@NotNull
	public ArrayList<MksServerInfo> getMksServers (final ProgressIndicator progress, final ArrayList<VcsException> errors, MksVcs vcs) {
		final ListServersAPI listServersAction = new ListServersAPI(errors, vcs);
		if (progress != null) {
			progress.setIndeterminate(true);
			progress.setText("Querying mks servers ...");
		}
		listServersAction.execute();
		if (listServersAction.foundError()) {
			reportErrors(listServersAction.errors, "encountered errors querying servers");
		}
		return listServersAction.servers;
	}

	private void reportErrors
			(List<VcsException> errors, String
					message) {
		for (VcsException error : errors) {
			LOGGER.warn(message, error);
		}
	}

	public boolean checkNeededServersAreOnlineAndReconnectIfNeeded(@NotNull Set<MksSandboxInfo> sandboxesToRefresh,
																 @NotNull ArrayList<MksServerInfo> servers, Project project) {
		Set<MksServerInfo> connectedServers = new HashSet<MksServerInfo>(servers);

		Set<MksServerInfo> serversNeedingReconnect = new HashSet<MksServerInfo>();
		for (MksSandboxInfo sandboxInfo : sandboxesToRefresh) {
			serversNeedingReconnect.add(MksServerInfo.fromHostAndPort(sandboxInfo.hostAndPort));
		}
		for (final MksServerInfo serverInfo : serversNeedingReconnect) {
			// request user and password
			MksServerInfo reconnectedServer = reconnect(project, serverInfo);
			if (reconnectedServer != null) {
				connectedServers.add(reconnectedServer);
			}
		}

		return  (connectedServers.size() == serversNeedingReconnect.size());
	}

	public MksServerInfo reconnect(Project project, MksServerInfo serverInfo) {
		MksServerInfo reconnectedServer = null;
		String host = serverInfo.host;
		String port = serverInfo.port;

		PasswordSafe passwordSafe = PasswordSafe.getInstance();
		String hostAndPort = serverInfo.toHostAndPort();
		final UserAndPassword userAndPassword = getUsernameAndPassword(hostAndPort, project);
		if (userAndPassword != null) {

			if (userAndPassword.user == null || userAndPassword.password == null) {
				try {
					Runnable runnable = new CredentialsInputRunnable(project, hostAndPort, userAndPassword);
					MksVcs.invokeOnEventDispatchThreadAndWait(runnable);
				} catch (VcsException e) {
					LOGGER.error(e);
				}
			}
			SiConnectCommandAPI command = new SiConnectCommandAPI(MksVcs.getInstance(project), host, port, userAndPassword.user, userAndPassword.password);
			command.execute();
			if (!command.foundError()) {
				String passwordKey = createPasswordKey(hostAndPort, userAndPassword.user);
				if (!command.foundError() && (command.getServer() != null)) {
					reconnectedServer = command.getServer();
					try {
						MksConfiguration configuration = ApplicationManager.getApplication().getComponent(MksConfiguration.class);
						configuration.addRememberedUsername(hostAndPort, userAndPassword.user);
						passwordSafe.storePassword(project, MksVcs.class, passwordKey, userAndPassword.password);
					} catch (PasswordSafeException e) {
						reportErrors(Arrays.asList(new VcsException(e)), "unable to store credentials for [" + passwordKey + "]");
					}
				} else {
					reportErrors(command.errors, "unable to connect to " + hostAndPort);
					try {
						passwordSafe.removePassword(project, MksVcs.class, passwordKey);
					} catch (PasswordSafeException e) {
						reportErrors(Arrays.asList(new VcsException(e)), "unable to discard credentials for [" + passwordKey + "]");
					}
				}
			}
		}
		return reconnectedServer;
	}

	/**
	 * search for previously used username for <code>hostAndPort</code> in the configuration, if present search
	 * passwordSafe for the password, otherwise request credentials from user
	 *
	 * @param hostAndPort
	 * @param project
	 * @return
	 */
	private UserAndPassword getUsernameAndPassword(final String hostAndPort, final Project project) {
		final UserAndPassword userAndPassword = new UserAndPassword();
		MksConfiguration configuration = ApplicationManager.getApplication().getComponent(MksConfiguration.class);
		Set<String> usernames = configuration.getRememberedUsernames(hostAndPort);
		if (usernames.isEmpty()) {
			try {
				Runnable runnable = new CredentialsInputRunnable(project, hostAndPort, userAndPassword);
				MksVcs.invokeOnEventDispatchThreadAndWait(runnable);
			} catch (VcsException e) {
				LOGGER.error(e);
				return null;
			}
		} else if (usernames.size() > 1) {
			LOGGER.warn("unexpected : more than one username remembered for " + hostAndPort);
		} else {
			String username = usernames.iterator().next();
			String passwordKey = createPasswordKey(hostAndPort, username);
			try {
				userAndPassword.user = username;
				userAndPassword.password = PasswordSafe.getInstance().getPassword(project, MksVcs.class, passwordKey);
				LOGGER.info("reconnecting to [" + hostAndPort + "] using [" + username + "]");
			} catch (PasswordSafeException e) {
				LOGGER.error("error querying password", e);
			}
		}
		return userAndPassword;
	}

	static class UserAndPassword {
		String user = null;
		String password = null;
	}

	private String createPasswordKey(String hostAndPort, String username) {
		return hostAndPort + ":" + username;
	}

	private static class CredentialsInputRunnable implements Runnable {
		private final Project project;
		private final String hostAndPort;
		private final UserAndPassword userAndPassword;

		public CredentialsInputRunnable(Project project, String hostAndPort, UserAndPassword userAndPassword) {
			this.project = project;
			this.hostAndPort = hostAndPort;
			this.userAndPassword = userAndPassword;
		}

		public void run() {

			LoginDialog dialog = new LoginDialog(WindowManager.getInstance().getFrame(project), hostAndPort);
			dialog.pack();
			dialog.setVisible(true);
			if (dialog.isCanceled()) {
				userAndPassword.user = userAndPassword.password = null;
			} else {
				userAndPassword.user = dialog.getUser();
				userAndPassword.password = dialog.getPassword();
			}
		}
	}

	public static class SICommands extends com.mks.api.commands.SICommands {
        public SICommands(Session session)
                throws APIException {
            super(session, true);
        }

        public Response siCheckOut(String cwd, String[] members, OptionList options)
                throws APIException {
            if (options == null)
                options = new OptionList();
            options.add(new Option("exclusive"));
            return super.siCheckOut(cwd, members, options);
        }

        public Response getSandboxMemberStatus(String cwd, String[] members)
                throws APIException {
            if (members == null) {
                return iiViewSandbox(cwd, true);
            }
            List fields = new ArrayList();
            fields.add("locked");
            fields.add("locker");
            fields.add("memberarchive");
            fields.add("memberrev");
            fields.add("name");
            fields.add("newrevdelta");
            fields.add("revsyncdelta");
            fields.add("type");
            fields.add("wfdelta");
            fields.add("workingrev");
            fields.add("merge");
            fields.add("frozen");
            fields.add("archiveshared");

            return getSandboxMemberStatus(cwd, members, fields);
        }

        private Response iiViewSandbox(String directory, boolean noSubInfo)
                throws APIException {
            if ((directory == null) || (directory.length() == 0)) {
                throw new InvalidCommandSelectionException("SICommands.iiViewSandbox: parameter 'directory' cannot be null or empty.");
            }

            Command cmd = new Command("ii", "viewsandbox");
            cmd.addOption(new Option("dir", directory));
            if (noSubInfo) {
                cmd.addOption(new Option("noSubInfo"));
            }
            return runAPICommand(cmd);
        }

        public void attachListener(String sandbox, int listenPort)
                throws APIException {
            Command cmd = new Command("si", "si.GenericSandboxListener");
            cmd.addOption(new Option("createlistener"));
            cmd.addOption(new Option("S", sandbox));
            cmd.addOption(new Option("listenerport", String.valueOf(listenPort)));

            runAPICommand(cmd, true);
        }

        public void detachListener(String sandbox)
                throws APIException {
            Command cmd = new Command("si", "si.GenericSandboxListener");
            cmd.addOption(new Option("removelistener"));
            cmd.addOption(new Option("S", sandbox));

            runAPICommand(cmd, true);
        }
    }
}
