package org.intellij.vcs.mks.actions;

import org.intellij.vcs.mks.MKSHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class LaunchSourceIntegrityAction extends AnAction {

	public LaunchSourceIntegrityAction() {
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		MKSHelper.launchClient();
	}
}
