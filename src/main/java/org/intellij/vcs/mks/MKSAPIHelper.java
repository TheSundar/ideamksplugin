package org.intellij.vcs.mks;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.mks.api.*;
import com.mks.api.response.APIException;
import com.mks.api.response.InvalidCommandSelectionException;
import com.mks.api.response.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MKSAPIHelper implements ApplicationComponent {
	private Session session;
	private SICommands siCommands;

	private static Session initializeMKSConnection()
			throws APIException {
		IntegrationPointFactory ipf = IntegrationPointFactory.getInstance();

		IntegrationPoint ip = ipf.createLocalIntegrationPoint();
		ip.setAutoStartIntegrityClient(true);

		Session session = ip.getCommonSession();
		session.setAutoReconnect(true);
		return session;
	}

	public Session getSession() {
		return session;
	}

	public void initComponent() {
		try {
			session = initializeMKSConnection();
		} catch (APIException e) {
			e.printStackTrace();
		}
	}

	public void disposeComponent() {
		try {
			session.release(true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (APIException e) {
			e.printStackTrace();
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
			siCommands= new SICommands(session);
		}
		return siCommands;
	}

	public static class SICommands extends com.mks.api.commands.SICommands
		   {
		     public SICommands(Session session)
		       throws APIException
		     {
		       super(session, true);
		     }

		     public Response siCheckOut(String cwd, String[] members, OptionList options)
		       throws APIException
		     {
		       if (options == null)
		         options = new OptionList();
		       options.add(new Option("exclusive"));
		       return super.siCheckOut(cwd, members, options);
		     }

		     public Response getSandboxMemberStatus(String cwd, String[] members)
		       throws APIException
		     {
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
		       throws APIException
		     {
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
		       throws APIException
		     {
		       Command cmd = new Command("si", "si.GenericSandboxListener");
		       cmd.addOption(new Option("createlistener"));
		       cmd.addOption(new Option("S", sandbox));
		       cmd.addOption(new Option("listenerport", String.valueOf(listenPort)));

		       runAPICommand(cmd, true);
		     }

		     public void detachListener(String sandbox)
		       throws APIException
		     {
		       Command cmd = new Command("si", "si.GenericSandboxListener");
		       cmd.addOption(new Option("removelistener"));
		       cmd.addOption(new Option("S", sandbox));

		       runAPICommand(cmd, true);
		     }
		   }
}
