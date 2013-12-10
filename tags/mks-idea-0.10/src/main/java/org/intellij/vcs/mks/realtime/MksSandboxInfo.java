package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class MksSandboxInfo implements Comparable<MksSandboxInfo> {
    public final String sandboxPath;
    public final String hostAndPort;
    public final String mksProject;
    public final String devPath;
    public final boolean isSubSandbox;
    protected final Logger LOGGER = Logger.getInstance(getClass().getName());
    final VirtualFile sandboxPjFile;
    int retries = 0;

    public MksSandboxInfo(@Nullable final VirtualFile sandboxPjFile, String hostAndPort,  boolean isSubSandbox, @NotNull String mksProject, @NotNull final String sandboxPath, @Nullable String devPath) {
        this.sandboxPjFile = sandboxPjFile;
        this.isSubSandbox = isSubSandbox;
        this.mksProject = mksProject;
        this.sandboxPath = sandboxPath;
        this.devPath = devPath;
        this.hostAndPort = hostAndPort.toLowerCase();
    }

    @Override
    public String toString() {
        return "MksSandbox[" + sandboxPath + "," + hostAndPort + (isSubSandbox ? ",subsandbox" : "") + "]";
    }

    /**
     * returns true if file belongs to this sandbox
     *
     * @param file
     * @return
     */
    public boolean contains(@NotNull VirtualFile file) {
        if (sandboxPjFile == null) {
            LOGGER.warn("contains : sandboxPjFile == null, " + toString());
            return false;
        }
        return VfsUtil.isAncestor(sandboxPjFile.getParent(), file, false);
    }

    @NotNull
    public String getRelativePath(@NotNull VirtualFile member) {
        final char separator = File.separatorChar;
        return getRelativePath(member, separator);
    }

    public String getRelativePath(VirtualFile member, char separator) {
        return VfsUtil.getRelativePath(member, sandboxPjFile.getParent(), separator);
    }

    @NotNull
    public VirtualFile getSandboxDir() {
        assert sandboxPjFile != null : "sandbox not initialized";
        return sandboxPjFile.getParent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MksSandboxInfo that = (MksSandboxInfo) o;

        return !(devPath != null ? !devPath.equals(that.devPath) : that.devPath != null)
                && hostAndPort.equals(that.hostAndPort)
                && mksProject.equals(that.mksProject)
                && sandboxPath.equals(that.sandboxPath);
    }

    @Override
    public int hashCode() {
        int result;
        result = sandboxPath.hashCode();
        result = 31 * result + hostAndPort.hashCode();
        result = 31 * result + mksProject.hashCode();
        result = 31 * result + (devPath != null ? devPath.hashCode() : 0);
        return result;
    }

    public int compareTo(final MksSandboxInfo sandboxInfo) {
        return sandboxPath.compareTo((sandboxInfo == null) ? null : sandboxInfo.sandboxPath);
    }
}
