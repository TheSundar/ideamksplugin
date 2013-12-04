package org.intellij.vcs.mks.sicommands;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Comparator;

public final class SandboxInfo {
    public static final Comparator<SandboxInfo> COMPARATOR = new Comparator<SandboxInfo>() {
        public int compare(SandboxInfo o1, SandboxInfo o2) {
            if (o1 == null) {
                return -1;
            } else {
                return o1.sandboxPath.compareTo(o2.sandboxPath);
            }
        }
    };
    public final String sandboxPath;
    public final String projectPath;
    public final String projectVersion;
    public final String serverHostAndPort;
    public final boolean subSandbox;
    public final String devPath;

    public SandboxInfo(String sandboxPath, String serverHostAndPort, String projectPath, String projectVersion) {
        this (sandboxPath, serverHostAndPort, projectPath, projectVersion, null, false);
    }
    public SandboxInfo(String sandboxPath, String serverHostAndPort, String projectPath, String projectVersion, String devPath) {
        this (sandboxPath, serverHostAndPort, projectPath, projectVersion, devPath, false);
    }

    private SandboxInfo(String sandboxPath, String serverHostAndPort, String projectPath, String projectVersion, String devPath, boolean subSandbox) {
        this.sandboxPath = sandboxPath;
        this.projectPath = projectPath;
        this.projectVersion = projectVersion;
        this.serverHostAndPort = serverHostAndPort;
        this.subSandbox = subSandbox;
        this.devPath = devPath;
    }

    public SandboxInfo(SandboxInfo parentSandbox, String sandboxPath, String subProjectPath, String projectVersion) {
        this (sandboxPath, parentSandbox.serverHostAndPort, parentSandbox.getServerFolder() + subProjectPath, projectVersion, null, true);
    }
    public SandboxInfo(SandboxInfo parentSandbox, String sandboxPath, String subProjectPath, String projectVersion, String devPath) {
        this (sandboxPath, parentSandbox.serverHostAndPort, parentSandbox.getServerFolder() + subProjectPath, projectVersion, devPath, true);
    }

    public String getServerFolder() {
        return projectPath.substring(0, projectPath.lastIndexOf('/') + 1);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SandboxInfo that = (SandboxInfo) o;

        if (subSandbox != that.subSandbox) {
            return false;
        }
        if (projectPath != null ? !projectPath.equals(that.projectPath) : that.projectPath != null) {
            return false;
        }
        if (projectVersion != null ? !projectVersion.equals(that.projectVersion) : that.projectVersion != null) {
            return false;
        }
        if (sandboxPath != null ? !sandboxPath.equals(that.sandboxPath) : that.sandboxPath != null) {
            return false;
        }
        if (serverHostAndPort != null ? !serverHostAndPort.equals(that.serverHostAndPort) :
                that.serverHostAndPort != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (sandboxPath != null ? sandboxPath.hashCode() : 0);
        result = 31 * result + (projectPath != null ? projectPath.hashCode() : 0);
        result = 31 * result + (projectVersion != null ? projectVersion.hashCode() : 0);
        result = 31 * result + (serverHostAndPort != null ? serverHostAndPort.hashCode() : 0);
        result = 31 * result + (subSandbox ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
