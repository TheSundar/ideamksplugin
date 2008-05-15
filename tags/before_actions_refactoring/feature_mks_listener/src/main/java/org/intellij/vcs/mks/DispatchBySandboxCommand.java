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
		for (VirtualFile file : virtualFiles) {
            if (file == null) {
                LOGGER.warn ("null virtual file passed to DispatchBySandboxCommand#execute");
            } else {
                TriclopsSiSandbox sandbox = mksVcs.getSandboxCache().findSandbox(file);
                if (sandbox == null) {
                    notInSandboxFiles.add(file);
                } else {
                    TriclopsSiSandbox existingSandbox = sandbox;
                    ArrayList<VirtualFile> managedFiles = filesBySandbox.get(existingSandbox);
                    if (managedFiles == null) {
                        managedFiles = new ArrayList<VirtualFile>();
                        filesBySandbox.put(existingSandbox, managedFiles);
                    }
                    managedFiles.add(file);
                }
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