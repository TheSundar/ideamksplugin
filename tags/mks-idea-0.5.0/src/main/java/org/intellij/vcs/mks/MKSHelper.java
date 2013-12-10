package org.intellij.vcs.mks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.*;

import java.util.HashMap;

public class MKSHelper {
    private static final Logger LOGGER = Logger.getInstance(MKSHelper.class.getName());

    private static TriclopsSiClient CLIENT;
    private static final Object mksLock = new Object();

    private static boolean isClientLoaded;
    private static boolean isClientValid;
    private static final HashMap<VirtualFile, TriclopsSiSandbox> SANDBOX_CACHE = new HashMap<VirtualFile, TriclopsSiSandbox>();

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
     * returns the mks sandbox in which the filepath is if any
     *
     * @param virtualFile
     * @return
     * @throws com.intellij.openapi.vcs.VcsException
     *          if the file is not in a sandbox
     */
    public static TriclopsSiSandbox getSandbox(VirtualFile virtualFile) throws VcsException {
        VirtualFile temp = virtualFile.getParent(), candidate = null;
        while (candidate == null && temp != null) {
            candidate = temp.findFileByRelativePath("project.pj");
            temp = temp.getParent();
        }
        if (candidate != null) {
            TriclopsSiSandbox sandbox = getSandBoxForProjectPj(candidate, virtualFile);
            sandbox.setIdeProjectPath(virtualFile.getPresentableUrl());
            return sandbox;

        } else {
            System.err.println("did not find sandbox for " + virtualFile);
            synchronized (mksLock) {
                if (!isClientValid) {
                    startClient();
                }
                try {
                    TriclopsSiSandbox sandbox = new TriclopsSiSandbox(CLIENT);
                    sandbox.setIdeProjectPath(virtualFile.getPresentableUrl());
                    sandbox.validate();
                    return sandbox;
                } catch (TriclopsException e) {
                    throw new VcsException("can't find sandbox for file[" + virtualFile.getPath() + "]" + "\n" + getMksErrorMessage());
                }
            }
        }
    }

    private static TriclopsSiSandbox getSandBoxForProjectPj(VirtualFile sandboxProjectFile, VirtualFile sandboxMember) throws VcsException {
//		System.out.println("looking up sandbox cache for " + sandboxProjectFile);
        synchronized (mksLock) {
            TriclopsSiSandbox sandbox = SANDBOX_CACHE.get(sandboxProjectFile);
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
                    throw new VcsException("can't find sandbox for file[" + sandboxProjectFile.getPath() + "]" + "\n" + getMksErrorMessage());
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("SANDBOX_CACHE adding  " + sandboxProjectFile + " for member " + sandboxMember);
                }
                SANDBOX_CACHE.put(sandboxProjectFile, sandbox);
            } else {
//				System.out.println("SANDBOX_CACHE cache hit");
            }
            return sandbox;
        }

    }

    public static void disconnect() throws VcsException {
        if (CLIENT != null) {
            try {
                CLIENT.disconnect();
            } catch (TriclopsException e) {
                throw new VcsException("error disconnecting ");
            }
            SANDBOX_CACHE.clear();
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
            return !"The command was cancelled.".equals(CLIENT.getErrorMessage());
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
            if (!isClientValid)
                startClient();
            if (CLIENT != null)
                CLIENT.aboutBox();
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
            if (!isClientValid)
                startClient();
            try {
                CLIENT.launch();
            } catch (TriclopsException e) {
            }
        }
    }

    public static void openMemberDiffView(TriclopsSiMembers members, int flags) throws TriclopsException {
        synchronized (mksLock) {
            if (CLIENT != null)
                members.openMemberDiffView(0);
        }

    }

    public static void openMemberArchiveView(TriclopsSiMembers members, int flags) throws TriclopsException {
        synchronized (mksLock) {
            if (CLIENT != null)
                members.openMemberArchiveView(0);
        }

    }

    public static void openMemberInformationView(TriclopsSiMembers members, int flags) throws TriclopsException {
        synchronized (mksLock) {
            if (CLIENT != null)
                members.openMemberInformationView(flags);
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
            if (CLIENT != null) sandbox.openSandboxView(null);
        }

    }

    public static void openConfigurationView() throws TriclopsException {
        synchronized (mksLock) {
            if (!isClientValid)
                startClient();
            if (CLIENT != null)
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
}
