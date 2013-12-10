package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MksChangePackage;
import org.intellij.vcs.mks.MksVcs;

import java.io.IOException;
import java.util.List;

/**
 * @author Thibaut Fagart
 */
public class RenameChangePackage extends SiCLICommand {
    private final MksChangePackage changePackage;

    public RenameChangePackage(List<VcsException> errors, MksVcs mksvcs, MksChangePackage changePackage, String newName) {
        super(errors, mksvcs, "editcp", "--summary", newName, changePackage.getId());
        this.changePackage = changePackage;
    }

    @Override
    public void execute() {
        try {
            executeCommand();
            String[] lines = commandOutput.split("\r\n");
            int start = 0;
            while (shoudIgnore(lines[start])) {
                // skipping connecting/reconnecting lines
                start++;
            }
            if (lines.length != (start + 1) || !lines[start].equals(changePackage.getId())) {
                String message = "unexpected command output {" + lines[start] + "}, expected {" + changePackage.getId() + "}";
                LOGGER.error(message);
                //noinspection ThrowableInstanceNeverThrown
                errors.add(new VcsException(message));
            }
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
    }
    @Override
    public String toString() {
        return "RenameChangePackage["+changePackage.getId()+"]";
    }

}
