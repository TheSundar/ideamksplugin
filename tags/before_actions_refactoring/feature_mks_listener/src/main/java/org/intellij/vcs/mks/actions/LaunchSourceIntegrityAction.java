package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.intellij.vcs.mks.MKSHelper;

public class LaunchSourceIntegrityAction extends AnAction {

	public LaunchSourceIntegrityAction() {
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		MKSHelper.launchClient();

	}
}
