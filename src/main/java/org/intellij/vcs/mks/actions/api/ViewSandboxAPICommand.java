package org.intellij.vcs.mks.actions.api;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.AbstractVcs;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

public class ViewSandboxAPICommand extends AbstractSingleTargetAPICommand {
	private final Logger LOGGER = Logger.getInstance(getClass().getName());

    @Override
    protected void perform(@NotNull MksSandboxInfo sandbox, String[] members) throws APIException {
        final CmdRunner runner =  MKSAPIHelper.getInstance().getSession().createCmdRunner();
        Command command = new Command(Command.SI);
        command.setCommandName("viewsandbox");
        command.addOption(new Option("gui"));
        command.addOption(new Option("sandbox", sandbox.sandboxPath));

        runner.execute(command);
    }


	@NotNull
	public String getActionName(@NotNull AbstractVcs vcs) {
		return MksBundle.message("action.view.sandbox");
	}
}
