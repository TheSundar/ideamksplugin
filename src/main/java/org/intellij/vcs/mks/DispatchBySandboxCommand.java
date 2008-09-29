package org.intellij.vcs.mks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thibaut Fagart
 */
public class DispatchBySandboxCommand {
	protected final Logger LOGGER = Logger.getInstance(getClass().getName());
	private final MksVcs mksVcs;
	private VirtualFile[] virtualFiles;
	protected Map<MksSandboxInfo, ArrayList<VirtualFile>> filesBySandbox =
			new HashMap<MksSandboxInfo, ArrayList<VirtualFile>>();
	protected ArrayList<VirtualFile> notInSandboxFiles = new ArrayList<VirtualFile>();
	/**
	 * if false, dispatching will be done using subsandboxes
	 */
	private boolean basedOnTopSanbox;

	public DispatchBySandboxCommand(MksVcs mksVcs, VirtualFile[] virtualFiles) {
		this(mksVcs, virtualFiles, true);
	}

	public DispatchBySandboxCommand(MksVcs mksVcs, VirtualFile[] virtualFiles, boolean topSandboxOnly) {
		this.mksVcs = mksVcs;
		this.virtualFiles = virtualFiles;
		this.basedOnTopSanbox = topSandboxOnly;
	}

	public void execute() {
		for (VirtualFile file : virtualFiles) {
			if (file == null) {
				LOGGER.warn("null virtual file passed to DispatchBySandboxCommand#execute");
			} else {

				final MksSandboxInfo sandbox = (basedOnTopSanbox) ? mksVcs.getSandboxCache().getSandboxInfo(file) :
						mksVcs.getSandboxCache().getSubSandbox(file);
				if (sandbox == null) {
					notInSandboxFiles.add(file);
				} else {
					ArrayList<VirtualFile> managedFiles = filesBySandbox.get(sandbox);
					if (managedFiles == null) {
						managedFiles = new ArrayList<VirtualFile>();
						filesBySandbox.put(sandbox, managedFiles);
					}
					managedFiles.add(file);
				}
			}
		}
		if (MksVcs.DEBUG) {
			MksVcs.LOGGER
					.debug("dispatched " + virtualFiles.length + " files to " + filesBySandbox.size() + " sandboxes");
		}
	}

	public Map<MksSandboxInfo, ArrayList<VirtualFile>> getFilesBySandbox() {
		return filesBySandbox;
	}

	public ArrayList<VirtualFile> getNotInSandboxFiles() {
		return notInSandboxFiles;
	}
}
