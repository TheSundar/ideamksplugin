package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Returns a list of all sandboxes registered on the system, including subsandboxes
 */
public class SandboxesCommand extends SiCLICommand {
    /**
     * output is of the form
     * $sandboxPath$ -> $project$[$sandboxtype$:$projectVersionOrDevPath$] ($server$:$port$)
     */
    private static final String patternString = "(.+) -> ([^\\[]+)(?:\\[([^:]+):([^:]+)\\])? \\((.+)\\)";
    private final Pattern pattern;
    private static final int SANDBOX_PATH_GROUP_IDX = 1;
    private static final int PROJECT_PATH_GROUP_IDX = 2;
    private static final int PROJECT_TYPE_GROUP_IDX = 3;
    private static final int PROJECT_VERSION_GROUP_IDX = 4;
    private static final int SERVER_GROUP_IDX = 5;
    private SandboxInfo currentTopSandbox = null;

    public final ArrayList<SandboxInfo> result = new ArrayList<SandboxInfo>();

    public SandboxesCommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration) {
        super(errors, mksCLIConfiguration, "sandboxes", "--displaySubs");
        pattern = Pattern.compile(patternString);
    }

    public void execute() {
        try {
            executeCommand();
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
            return;
        }
        BufferedReader reader = new BufferedReader(new StringReader(commandOutput));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches()) {
                    LOGGER.error(
                            "unexpected command output {" + line + "}, expected something matching " + patternString,
                            "");
                    // ignoring line
                } else {
                    String sandboxPath = matcher.group(SANDBOX_PATH_GROUP_IDX);
                    String projectPath = matcher.group(PROJECT_PATH_GROUP_IDX);
                    String projectType = matcher.group(PROJECT_TYPE_GROUP_IDX);
                    String projectVersion = matcher.group(PROJECT_VERSION_GROUP_IDX);
                    String serverHostAndPort = matcher.group(SERVER_GROUP_IDX);
                    final SandboxInfo info =
                            resolveSandbox(sandboxPath, serverHostAndPort, projectPath, projectVersion);
                    result.add(info);
                }
            }
        } catch (IOException e) {
            LOGGER.error("error reading output " + commandOutput, e);
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }

    }

    private SandboxInfo resolveSandbox(String sandboxPath, String serverHostAndPort, String projectPath,
                                       String projectVersion) {
        boolean isSubSandbox = isSubSandbox(projectPath);
        SandboxInfo info;
        if (isSubSandbox) {
            if (currentTopSandbox == null) {
                throw new IllegalStateException("encountering a subsandbox without its containing sandbox");
            }
            info = new SandboxInfo(currentTopSandbox, sandboxPath, projectPath, projectVersion);
        } else {
            if (projectPath.indexOf('/') < 0) {
                throw new IllegalStateException("projectPath [" + projectPath + "] does not contain /");
            }
            info = new SandboxInfo(sandboxPath, serverHostAndPort, projectPath, projectVersion);
            currentTopSandbox = info;
        }
        return info;
    }

    private boolean isSubSandbox(String projectPath) {
        return !(isUnixAbsolutePath(projectPath) || isWindowAbsolutePath(projectPath));
    }

    private boolean isWindowAbsolutePath(String projectPath) {
        return projectPath.length() > 1 && projectPath.charAt(1) == ':' && projectPath.charAt(2) == '/';
    }

    private boolean isUnixAbsolutePath(String projectPath) {
        return projectPath.charAt(0) == '/';
    }


    public static final class SandboxInfo {
        public final String sandboxPath;
        public final String projectPath;
        public final String projectVersion;
        public final String serverHostAndPort;
        public final boolean subSandbox;

        public SandboxInfo(String sandboxPath, String serverHostAndPort, String projectPath, String projectVersion) {
            this.sandboxPath = sandboxPath;
            this.serverHostAndPort = serverHostAndPort;
            this.projectPath = projectPath;
            this.projectVersion = projectVersion;
            this.subSandbox = false;

        }

        public SandboxInfo(SandboxInfo parentSandbox, String sandboxPath, String subProjectPath,
                           String projectVersion) {
            this.sandboxPath = sandboxPath;
            this.projectPath = parentSandbox.getServerFolder() + subProjectPath;
            this.serverHostAndPort = parentSandbox.serverHostAndPort;
            this.projectVersion = projectVersion;
            this.subSandbox = true;
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
            if (serverHostAndPort != null ? !serverHostAndPort.equals(that.serverHostAndPort) : that.serverHostAndPort != null) {
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
    }
}
