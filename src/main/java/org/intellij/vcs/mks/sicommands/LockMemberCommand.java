package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * This command allows to lock a varying number of members.
 * All the members must belong to the same sandbox or the command will fail
 */
public class LockMemberCommand extends SiCLICommand {
    @NotNull
    private final String[] members;
    @org.jetbrains.annotations.NonNls
    public static final String COMMAND = "lock";

    public LockMemberCommand(List<VcsException> errors, MksCLIConfiguration mksCLIConfiguration, @NotNull MksSandboxInfo sandbox, @NotNull MksChangePackage changePackage, String... members) {
        super(errors, mksCLIConfiguration, COMMAND, createArray(members, "--sandbox=" + sandbox.sandboxPath, "--nobranch", "--nobranchvariant", "--cpid=" + changePackage.getId()));
        assert members.length > 0 : "need to specify which member to lock";
        setWorkingDir(new File(VcsUtil.getFilePath(sandbox.sandboxPath).getParentPath().getPath()));
        this.members = members;
    }

    private static String[] createArray(String[] members, String... otherStrings) {
        String[] array = new String[otherStrings.length + members.length];
        System.arraycopy(otherStrings, 0, array, 0, otherStrings.length);
        System.arraycopy(members, 0, array, otherStrings.length, members.length);
        return array;
    }

    public void execute() {
        try {
            super.executeCommand();
            // todo verifier que le lock s'est bien passé
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
    }

    protected void handleErrorOutput(String errorOutput) {
        if (exitValue == 0) {
            if (errorOutput.length() == 0) {
                return;
            } else {
                BufferedReader reader = new BufferedReader(new StringReader(errorOutput));
                String line1 = null;
                try {
                    line1 = reader.readLine();
                    if (reader.readLine() == null) {
                        return;
                    }
                    if (line1.startsWith("Locking revision of member...")) {
                        return;
                    }
                } catch (IOException e) {
                    super.handleErrorOutput(errorOutput);
                }
            }
        }
        super.handleErrorOutput(errorOutput);
    }
}
