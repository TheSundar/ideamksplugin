package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.Command;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ViewChangePackageAPICommand extends SiAPICommand {
	private final String changePackageId;
	public ViewChangePackageAPICommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration, MksChangePackage cp) {
		this (errors, mksCLIConfiguration, cp.getId());
	}
	public ViewChangePackageAPICommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration, String cpid) {
		super(errors, "viewcp", mksCLIConfiguration);
		this.changePackageId = cpid;
	}

	@Override
	public void execute() {
		long start = System.currentTimeMillis();
		try {
			getAPIHelper().getSICommands().siChangePackageView(changePackageId);
		} catch (APIException e) {
			errors.add(new VcsException(e));
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
}
