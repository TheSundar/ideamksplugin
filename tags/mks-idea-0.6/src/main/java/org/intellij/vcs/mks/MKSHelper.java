package org.intellij.vcs.mks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.intellij.vcs.mks.realtime.SandboxCache;
import org.intellij.vcs.mks.sicommands.ListSandboxes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiClient;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiProject;
import mks.integrations.common.TriclopsSiSandbox;

public class MKSHelper {
    private static final Logger LOGGER = Logger.getInstance(MKSHelper.class.getName());

    private static TriclopsSiClient CLIENT;
    private static final Object mksLock = new Object();
    //	protected static final String CLIENT_LIBRARY_NAME = "mkscmapi";

    private static boolean isClientLoaded;
    private static boolean isClientValid;
    private static long LAST_SANDBOX_CACHE_REFRESH = 0;
    private static final long SECOND = 1000;

    private static long SANDBOX_REFRESH_IF_OLDER = 5 * SECOND;

    /**
     * stores an association LocalFS directory=>TriclopsSiSandbox
     */
    private static class LegacySandboxCache implements SandboxCache {
        private final Object lock = new Object();
        /**
         * sandboxFolder virtualFile => TriclopsSiSandbox
         */
        private final HashMap<VirtualFile, TriclopsSiSandbox> sandboxByFolder = new HashMap<VirtualFile, TriclopsSiSandbox>();
        private final HashMap<VirtualFile, VirtualFile> folderBySandboxFile = new HashMap<VirtualFile, VirtualFile>();

        public boolean isSandboxProject(@NotNull VirtualFile virtualFile) {
            return folderBySandboxFile.containsKey(virtualFile);
        }

        @Nullable
        public TriclopsSiSandbox findSandbox(@NotNull VirtualFile virtualFile) {
            TriclopsSiSandbox sandbox = null;
            VirtualFile cursorDir = (virtualFile.isDirectory() ? virtualFile : virtualFile.getParent());
            for (; cursorDir != null && sandbox == null; cursorDir = cursorDir.getParent())
            {
                sandbox = sandboxByFolder.get(cursorDir);
            }
            return sandbox;
        }

        public void addSandboxPath(@NotNull String sandboxPath) {
            synchronized (lock) {
                VirtualFile sandboxVFile = VcsUtil.getVirtualFile(sandboxPath);
                VirtualFile sandboxFolder;
                if (sandboxVFile != null) {
                    sandboxFolder = sandboxVFile.getParent();
                    try {
                        TriclopsSiSandbox sandbox = createSandbox(sandboxPath);
                        sandboxByFolder.put(sandboxFolder, sandbox);
                        folderBySandboxFile.put(sandboxVFile, sandboxFolder);
                    } catch (TriclopsException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        LOGGER.error("invalid sandbox ? (" + sandboxPath + ")");
                    }
                } else {
                    LOGGER.error("unable to find the virtualFile for " + sandboxPath);
                }
            }
        }

        public void clear() {
            synchronized (lock) {
                sandboxByFolder.clear();
                folderBySandboxFile.clear();
            }
        }
    }

    // todo make it project related : attach it to the MksVcs
    private static final SandboxCache sandboxCache = new LegacySandboxCache();

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

    /**
     * @param virtualFile a mks controlled file
     * @param mksVcs      the vcs (holding the project)
     * @return the mks sandbox in which the filepath is if any
     * @throws com.intellij.openapi.vcs.VcsException
     *          if the file is not in a sandbox
     */
    @NotNull
    public static TriclopsSiSandbox getSandbox(@NotNull VirtualFile virtualFile, MksVcs mksVcs) throws VcsException {
        TriclopsSiSandbox sandbox = findSandboxInCache(virtualFile);
        if (sandbox == null) {
            refreshSandboxCache(mksVcs);
            sandbox = findSandboxInCache(virtualFile);
            if (sandbox == null) {
//				System.out.println("can't find sandbox for file[" + virtualFile.getPath() + "]");
                throw new VcsException("can't find sandbox for file[" + virtualFile.getPath() + "]");
            }
        }
        return sandbox;
    }

    @Nullable
    private static TriclopsSiSandbox findSandboxInCache(@NotNull VirtualFile virtualFile) {
        return sandboxCache.findSandbox(virtualFile);
    }

    private static void refreshSandboxCache(MksVcs mksVcs) {
        if (System.currentTimeMillis() > LAST_SANDBOX_CACHE_REFRESH + SANDBOX_REFRESH_IF_OLDER) {
            ListSandboxes listSandboxes = new ListSandboxes(new ArrayList<VcsException>(), mksVcs);
            listSandboxes.execute();
            if (listSandboxes.foundError()) {
                LOGGER.warn("Sandbox list only partially refreshed due to ");
                for (VcsException error : listSandboxes.errors) {
                    LOGGER.error(error.getMessage());
                }
            }
            sandboxCache.clear();
            for (String sandbox : listSandboxes.sandboxes) {
                VirtualFile virtualFile = VcsUtil.getVirtualFile(sandbox);
                if (virtualFile == null) {
                    LOGGER.error("No VirtualFile for " + sandbox);
                    continue;
                }
//                PsiDirectory sandboxPsiDirectory = PsiManager.getInstance(mksVcs.getProject()).findDirectory(virtualFile);
                if (mksVcs.getProject().getProjectScope().contains(virtualFile)) {
                    // ok sandbox in project path
                    sandboxCache.addSandboxPath(sandbox);
                } else {
                    LOGGER.info("ignoring out-of-project sandbox " + sandbox);
                }
            }
            LAST_SANDBOX_CACHE_REFRESH = System.currentTimeMillis();

        }
    }

/*
	private static TriclopsSiSandbox getSandBoxForProjectPj(String sandboxPath, VirtualFile sandboxMember) throws VcsException {
//		System.out.println("looking up sandbox cache for " + sandboxPath);
		synchronized (mksLock) {
			TriclopsSiSandbox sandbox = SANDBOX_CACHE.get(sandboxPath);
			if (sandbox == null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("SANDBOX_CACHE cache miss");
				}
				if (!isClientValid) {
					startClient();
				}
				try {
					sandbox = new TriclopsSiSandbox(CLIENT);
					sandbox.setIdeProjectPath(sandboxMember.getPresentableUrl());
					sandbox.validate();

				} catch (TriclopsException e) {
					throw new VcsException("can't find sandbox for file[" + sandboxPath + "]" + "\n" + getMksErrorMessage());
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("SANDBOX_CACHE adding  " + sandboxPath + " for member " + sandboxMember);
				}
				SANDBOX_CACHE.put(sandbox.getPath(), sandbox);
			} else {
//				System.out.println("SANDBOX_CACHE cache hit");
			}
			return sandbox;
		}

	}
*/

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

    public static String[] getProjectMembers(TriclopsSiSandbox sandbox) throws TriclopsException {
        synchronized (mksLock) {
            TriclopsSiProject project = new TriclopsSiProject(CLIENT);
            project.setPath(sandbox.getSandboxProject());
            return project.getMemberList();
        }
    }

    /**
     * Ignores the files materializing MKS projects
     *
     * @param sandbox     the sandbox containing the file
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

}
