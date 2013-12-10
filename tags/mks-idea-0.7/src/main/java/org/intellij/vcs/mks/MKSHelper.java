package org.intellij.vcs.mks;

import java.io.File;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiClient;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;

public class MKSHelper {
	private static final Logger LOGGER = Logger.getInstance(MKSHelper.class.getName());

	private static TriclopsSiClient CLIENT;
	private static final Object mksLock = new Object();
	//	protected static final String CLIENT_LIBRARY_NAME = "mkscmapi";

	private static boolean isClientLoaded;
	private static boolean isClientValid;

	public static void getMembersStatus(TriclopsSiMembers members) throws TriclopsException {
		synchronized (mksLock) {
			if (members.getNumMembers() > 0) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("queryStatuses {" + members.getMember(0).getPath() + "}");
				}
			} else {
				LOGGER.debug("queryStatuses {emptyMembers}");
			}
			members.getMembersStatus();
		}
	}

	public static synchronized String getMksErrorMessage() {
		return "Mks.errorMessage = " + CLIENT.getErrorMessage() +
				"\nMks.extendedErrorMessage = " + CLIENT.getExtendedErrorMessage();

	}

	public static void disconnect() throws VcsException {
		if (CLIENT != null) {
			try {
				CLIENT.disconnect();
			} catch (TriclopsException e) {
				throw new MksVcsException("error disconnecting", e);
			}
		}

	}

	public static void startClient() {
		if (!isClientLoaded) {
			LOGGER.debug("Starting MKS client");
			try {
				// todo would it be needed to allow configuration to locate that dll ?
				System.loadLibrary("mkscmapi");
				isClientLoaded = true;
				CLIENT = new TriclopsSiClient();
				if (!CLIENT.isIntegrityClientRunning()) {
					CLIENT.initialize("IntelliJ IDEA", 1, 1);
				}
				isClientValid = true;
			} catch (Throwable t) {
				LOGGER.error("FAILED Starting MKS client", t);
				isClientLoaded = false;
				isClientValid = false;
			}
		}
	}


	public static TriclopsSiMembers createMembers(TriclopsSiSandbox sandbox) {
		return new TriclopsSiMembers(CLIENT, sandbox);
	}

	public static boolean isLastCommandCancelled() {
		synchronized (mksLock) {
			return "The command was cancelled.".equals(CLIENT.getErrorMessage());
		}
	}

	public static void checkoutMembers(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.checkoutMembers(flags);
				getMembersStatus(members);
			}
		}
	}

	public static void checkinMembers(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.checkinMembers(flags);
				getMembersStatus(members);
			}
		}

	}

	public static void aboutBox() throws TriclopsException {
		synchronized (mksLock) {
			if (!isClientValid) {
				startClient();
			}
			if (CLIENT != null) {
				CLIENT.aboutBox();
			}
		}

	}

	public static void addMembers(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.addMembers(flags);
				getMembersStatus(members);
			}
		}

	}

	public static void dropMembers(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.dropMembers();
				getMembersStatus(members);
			}
		}

	}

	public static void launchClient() {
		synchronized (mksLock) {
			if (!isClientValid) {
				startClient();
			}
			try {
				CLIENT.launch();
			} catch (TriclopsException e) {
				LOGGER.error("error while launching MKS Client", e);
			}
		}
	}

	public static void openMemberDiffView(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.openMemberDiffView(0);
			}
		}

	}

	public static void openMemberArchiveView(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.openMemberArchiveView(0);
			}
		}

	}

	public static void openMemberInformationView(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.openMemberInformationView(flags);
			}
		}

	}

	public static void resyncMembers(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.resyncMembers(flags);
				getMembersStatus(members);
			}
		}

	}

	public static void revertMembers(TriclopsSiMembers members, int flags) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				members.revertMembers(flags);
				getMembersStatus(members);
			}
		}

	}

	public static void viewSandbox(TriclopsSiSandbox sandbox) throws TriclopsException {
		synchronized (mksLock) {
			if (CLIENT != null) {
				sandbox.openSandboxView(null);
			}
		}

	}

	public static void openConfigurationView() throws TriclopsException {
		synchronized (mksLock) {
			if (!isClientValid) {
				startClient();
			}
			if (CLIENT == null) {
				return;
			}
			CLIENT.openConfigurationView();
		}

	}

	/**
	 * Ignores the files materializing MKS projects
	 *
	 * @param sandbox	 the sandbox containing the file
	 * @param virtualFile the file to be tested for ignore
	 * @return whether this file is ignorable according to MKS
	 */
	public static boolean isIgnoredFile(TriclopsSiSandbox sandbox, VirtualFile virtualFile) {
		return VfsUtil.virtualToIoFile(virtualFile).equals(new File(sandbox.getPath()));
	}

	public static TriclopsSiSandbox createSandbox(String pjFilePath) throws TriclopsException {
		synchronized (mksLock) {
			TriclopsSiSandbox siSandbox = new TriclopsSiSandbox(CLIENT);
			siSandbox.setPath(pjFilePath);
			siSandbox.setIdeProjectPath(pjFilePath);
			siSandbox.validate();
			return siSandbox;
		}
	}

	public static TriclopsSiMembers createMembers(MksSandboxInfo key) {
		return createMembers(key.getSiSandbox());
	}

	public static String getRelativePath(FilePath filePath, FilePath parentPath) {
		return filePath.getPath().substring(parentPath.getPath().length() + 1);
	}
}
