package org.intellij.vcs.mks.actions.triclops;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import org.intellij.vcs.mks.*;
import org.intellij.vcs.mks.actions.MksCommand;
import org.intellij.vcs.mks.realtime.MksNativeSandboxInfo;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public abstract class AbstractTriclopsCommand implements MksCommand {
	/**
	 * precondition : ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(mksvcs,
	 * vFiles)
	 *
	 * @param vcs   the vcs for this project
	 * @param files a list of files, all should be under mks, but do not to have
	 *              to be under the same sandbox
	 * @return an array of same-sandbox TriclopsSiMembers
	 * @throws com.intellij.openapi.vcs.VcsException
	 *          if anything goes wrong
	 */
	@NotNull
	protected TriclopsSiMembers[] createSiMembers(@NotNull MksVcs vcs, @NotNull VirtualFile... files) throws
			VcsException {
		try {
			DispatchBySandboxCommand dispatchAction =
					new DispatchBySandboxCommand(vcs, files);
			dispatchAction.execute();

			Map<MksSandboxInfo, ArrayList<VirtualFile>> filesBysandbox = dispatchAction.getFilesBySandbox();
			TriclopsSiMembers[] result = new TriclopsSiMembers[filesBysandbox.size()];
			int i = 0;
			for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : filesBysandbox.entrySet()) {
                if (entry.getKey() instanceof MksNativeSandboxInfo) {
                    TriclopsSiMembers members = MKSHelper.createMembers(((MksNativeSandboxInfo) entry.getKey()));
                    result[i++] = members;
                    for (VirtualFile virtualFile : entry.getValue()) {
                        members.addMember(new TriclopsSiMember(virtualFile.getPresentableUrl()));
                    }
                    MKSHelper.getMembersStatus(members);
                }                     else {
                    throw new UnsupportedOperationException("native");
                }
            }
			return result;
		} catch (TriclopsException e) {
			throw new MksVcsException(MksBundle.message("unable.to.obtain.file.status"), e);
		}

	}

}
