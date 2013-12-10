package org.intellij.vcs.mks.sicommands;

import java.io.IOException;
import java.util.List;

import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.vcs.VcsException;

/**
 * runs si viewcp --gui $cpid$
 */
public class ViewChangePackageCommand extends SiCLICommand {

    public ViewChangePackageCommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration,
        MksChangePackage cp) {
        super(errors, mksCLIConfiguration, "viewcp", false, "--gui", cp.getId());
    }

    @Override
    public void execute() {
        try {
            executeCommand();
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
    }
}
