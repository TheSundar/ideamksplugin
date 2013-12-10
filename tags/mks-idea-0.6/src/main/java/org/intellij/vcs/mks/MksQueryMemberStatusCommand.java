package org.intellij.vcs.mks;

import java.util.ArrayList;
import java.util.List;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;

/**
 * @author Thibaut Fagart
 */
public class MksQueryMemberStatusCommand extends AbstractMKSCommand {
    private final TriclopsSiSandbox sandbox;
    private ArrayList<VirtualFile> files;
    public TriclopsSiMembers triclopsSiMembers;


    public MksQueryMemberStatusCommand(List<VcsException> errors, TriclopsSiSandbox sandbox, ArrayList<VirtualFile> files) {
        super(errors);
        this.sandbox = sandbox;
        this.files = files;
    }

    @Override
    public void execute() {
        try {
            triclopsSiMembers = super.queryMksMemberStatus(files, sandbox);
        } catch (TriclopsException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new MksVcsException("unable to get status" + "\n" + MksVcs.getMksErrorMessage(), e));
        }
    }
}
