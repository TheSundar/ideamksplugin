package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsSiSandbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thibaut Fagart
 */
public class DispatchBySandboxCommand extends AbstractMKSCommand {
	private final MksVcs mksVcs;
	private VirtualFile[] virtualFiles;
	protected Map<TriclopsSiSandbox, ArrayList<VirtualFile>> filesBySandbox =
		new HashMap<TriclopsSiSandbox, ArrayList<VirtualFile>>();
	protected ArrayList<VirtualFile> notInSandboxFiles = new ArrayList<VirtualFile>();

	public DispatchBySandboxCommand(MksVcs mksVcs, List<VcsException> errors, VirtualFile[] virtualFiles) {
		super(errors);
		this.mksVcs = mksVcs;
		this.virtualFiles = virtualFiles;
	}

	@Override
	public void execute() {
		Map<String, TriclopsSiSandbox> sandboxesByPath = new HashMap<String, TriclopsSiSandbox>();
		for (VirtualFile file : virtualFiles) {
			try {
				TriclopsSiSandbox sandbox = MKSHelper.getSandbox(file, mksVcs);
				TriclopsSiSandbox existingSandbox = sandboxesByPath.get(sandbox.getPath());
				if (existingSandbox == null) {
					existingSandbox = sandbox;
					sandboxesByPath.put(existingSandbox.getPath(), existingSandbox);
				}
				ArrayList<VirtualFile> managedFiles = filesBySandbox.get(existingSandbox);
				if (managedFiles == null) {
					managedFiles = new ArrayList<VirtualFile>();
					filesBySandbox.put(existingSandbox, managedFiles);
				}
				managedFiles.add(file);
			} catch (VcsException e) {
				MksVcs.LOGGER.debug("File not in sand box " + file.getPresentableUrl() + "\n" + MksVcs.getMksErrorMessage());
				notInSandboxFiles.add(file);
				//					errors.add(new VcsException(e));
			}
		}
		if (MksVcs.DEBUG) {
			MksVcs.LOGGER.debug("dispatched " + virtualFiles.length + " files to " + filesBySandbox.size() + " sandboxes");
		}
	}

	public Map<TriclopsSiSandbox, ArrayList<VirtualFile>> getFilesBySandbox() {
		return filesBySandbox;
	}

	public ArrayList<VirtualFile> getNotInSandboxFiles() {
		return notInSandboxFiles;
	}
}
