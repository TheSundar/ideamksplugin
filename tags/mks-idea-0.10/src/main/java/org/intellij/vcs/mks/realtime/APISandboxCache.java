package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksConfiguration;
import org.intellij.vcs.mks.sicommands.api.SandboxesCommandAPI;
import org.intellij.vcs.mks.sicommands.SandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class APISandboxCache extends AbstractSandboxCacheImpl implements ProjectComponent {

    public APISandboxCache(Project project) {
        super(project);
    }

    @Override
    protected MksSandboxInfo createSandboxInfo(String sandboxPath, String mksHostAndPort, String mksProject, String devPath, boolean isSubSandbox, VirtualFile sandboxVFile) {
        return new MksSandboxInfo(sandboxVFile, mksHostAndPort, isSubSandbox, mksProject, sandboxPath, devPath);
    }

    @Override
    protected void addRejected(MksSandboxInfo sandbox) {
    }

    @Override
    public void projectOpened() {
        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        } else {
            refresh();
        }

    }

    private void refresh() {
        MksCLIConfiguration configuration = getConfiguration();
        SandboxesCommandAPI command = new SandboxesCommandAPI(new ArrayList<VcsException>(), configuration);
        command.execute();
        this.release();
        for (SandboxInfo sandboxInfo : command.result) {
            this.addSandboxPath(sandboxInfo.sandboxPath, sandboxInfo.serverHostAndPort, sandboxInfo.projectPath,
                    sandboxInfo.devPath, sandboxInfo.subSandbox);
        }
    }

    protected MksCLIConfiguration getConfiguration() {
        return ApplicationManager.getApplication().getComponent(MksConfiguration.class);
    }

    @Override
    public void projectClosed() {
        release();
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MKSSandboxCache";
    }
}
