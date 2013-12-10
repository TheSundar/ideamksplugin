package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.actions.StandardVcsGroup;
import org.intellij.vcs.mks.MksVcs;

public class MksGroup extends StandardVcsGroup {
    static {
//        Logger.getInstance(MksGroup.class.getName()).error(
//        "MksGroup classloader : "+MksGroup.class.getClassLoader()
//            +", ActionGroup classloader :"+ActionGroup.class.getClassLoader());
    }

    public MksGroup() {
    }

    public AbstractVcs getVcs(Project project) {
        return (MksVcs) MksVcs.getInstance(project);
    }
}
