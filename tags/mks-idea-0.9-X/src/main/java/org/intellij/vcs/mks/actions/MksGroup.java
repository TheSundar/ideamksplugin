package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.actions.StandardVcsGroup;
import org.intellij.vcs.mks.MksVcs;

public class MksGroup extends StandardVcsGroup {
	public MksGroup() {
	}

	public AbstractVcs getVcs(Project project) {
		return MksVcs.getInstance(project);
	}
}
