package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.sicommands.cli.GetContentRevision;
import org.intellij.vcs.mks.sicommands.cli.SiCLICommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author Thibaut Fagart
 */
public class MksContentRevision implements ContentRevision {

	final VcsRevisionNumber myRevision;
	private MksVcs mksvcs;
	final FilePath myFile;

	public MksContentRevision(@NotNull MksVcs mksvcs, @NotNull FilePath myFile, @NotNull VcsRevisionNumber myRevision) {
		this.mksvcs = mksvcs;
		this.myFile = myFile;
		this.myRevision = myRevision;
	}

	@Nullable
	public String getContent() throws VcsException {
		if (VcsRevisionNumber.NULL.equals(myRevision)) {
			return null;
		} else {
			GetContentRevision getRevisionCommand = executeGetRevisionCommand();
			if (getRevisionCommand.foundError()) {
				VcsException vcsException = getRevisionCommand.errors.get(getRevisionCommand.errors.size() - 1);
				if (SiCLICommand.UNABLE_TO_RECONNECT_TO_MKS_SERVER.equals(vcsException.getMessage())) {
					getRevisionCommand = reconnectAndRetry();
				}
			}
			if (getRevisionCommand.foundError()) {
				throw getRevisionCommand.errors.get(getRevisionCommand.errors.size() - 1);
			}
			return getRevisionCommand.getContent();
		}
	}

	private GetContentRevision executeGetRevisionCommand() {
		GetContentRevision getRevisionCommand = new GetContentRevision(new ArrayList<VcsException>(), mksvcs,
				this.getRevisionNumber(), this.getFile().getPath());
		getRevisionCommand.execute();
		return getRevisionCommand;
	}

	private GetContentRevision reconnectAndRetry() throws VcsException {
		MksSandboxInfo sandboxInfo = mksvcs.getSandboxCache().getSandboxInfo(myFile.getVirtualFile());
		if (sandboxInfo == null) {
			throw new VcsException("unable to find sandbox  for [" + myFile + "]");
		}
		MKSAPIHelper.getInstance().reconnect(mksvcs.getProject(), MksServerInfo.fromHostAndPort(sandboxInfo.hostAndPort));
//						checkNeededServersAreOnlineAndReconnectIfNeeded(sandboxInfos, MKSAPIHelper.getInstance().getMksServers(null, new ArrayList<VcsException>(), mksvcs), mksvcs.getProject());
		return executeGetRevisionCommand();
	}

	@NotNull
	public FilePath getFile() {
		return myFile;
	}

	@NotNull
	public VcsRevisionNumber getRevisionNumber() {
		return myRevision;
	}

	@Override
	public String toString() {
		return "MksContentRevision[" + getFile() + ":" + getRevisionNumber() + "]";
	}
}
