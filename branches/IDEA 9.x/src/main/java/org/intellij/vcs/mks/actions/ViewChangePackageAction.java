package org.intellij.vcs.mks.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.intellij.vcs.mks.MksChangeListAdapter;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.sicommands.ViewChangePackageCommand;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeList;

/**
 * Opens any selected change package in the Native MKS client.
 * <p/>
 * This action is disabled if no mks backed change list is selected.
 */
public class ViewChangePackageAction extends AnAction {
    @Override public void update(final AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();
        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        final MksVcs mksVcs = MksVcs.getInstance(project);
        e.getPresentation().setEnabled(!getSelectedChangePackages(dataContext, mksVcs).isEmpty());
    }

    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        final DataContext dataContext = anActionEvent.getDataContext();
        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        final MksVcs mksVcs = MksVcs.getInstance(project);

        final ArrayList<VcsException> errors = new ArrayList<VcsException>();
        for (final MksChangePackage aPackage : getSelectedChangePackages(dataContext, mksVcs)) {
            new ViewChangePackageCommand(errors, mksVcs, aPackage).execute();
        }
    }

    @NotNull
    private List<MksChangePackage> getSelectedChangePackages(@NotNull final DataContext dataContext,
        @NotNull final MksVcs mksVcs) {
        final ChangeList[] changeLists = VcsDataKeys.CHANGE_LISTS.getData(dataContext);
        if (null == changeLists) {
            return Collections.emptyList();
        }
        final List<MksChangePackage> changePackages = new ArrayList<MksChangePackage>(changeLists.length);


        final MksChangeListAdapter adapter = mksVcs.getChangeListAdapter();
        for (final ChangeList changeList : changeLists) {
            final MksChangePackage aPackage = adapter.getMksChangePackage(changeList.getName());
            if (null != aPackage) {
                changePackages.add(aPackage);
            }
        }
        return changePackages;
    }
}
