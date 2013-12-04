package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.*;
import com.mks.api.response.APIException;
import com.mks.api.response.InvalidCommandSelectionException;
import com.mks.api.response.Response;
import org.intellij.vcs.mks.AbstractMKSCommand;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SiAPICommand extends AbstractMKSCommand {


	protected SiAPICommand(@NotNull List<VcsException> errors, @NotNull String command, @NotNull MksCLIConfiguration mksCLIConfiguration) {
		super(errors, command, mksCLIConfiguration);
	}


    protected MKSAPIHelper getAPIHelper() {
        return MKSAPIHelper.getInstance();
    }
}
