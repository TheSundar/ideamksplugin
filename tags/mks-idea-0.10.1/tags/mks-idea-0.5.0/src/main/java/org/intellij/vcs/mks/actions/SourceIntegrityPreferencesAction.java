package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mks.integrations.common.TriclopsException;
import org.intellij.vcs.mks.MKSHelper;

public class SourceIntegrityPreferencesAction extends AnAction {

    public SourceIntegrityPreferencesAction() {
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            MKSHelper.openConfigurationView();
        }
        catch (TriclopsException e) {
        }
    }
}
