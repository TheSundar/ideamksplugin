package org.intellij.vcs.mks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.sicommands.RenameChangePackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thibaut Fagart
 */
class MksChangeListAdapter extends ChangeListAdapter {
	private final Logger logger = Logger.getInstance(getClass().getName());
	@NotNull
	private final MksVcs mksVcs;

	public MksChangeListAdapter(@NotNull MksVcs mksVcs) {
		this.mksVcs = mksVcs;
	}

	private final Map<String, String> changeListNameByChangePackageId = new HashMap<String, String>();
	private final Map<String, String> changePackageIdByChangeListName = new HashMap<String, String>();
	private final Map<String, MksChangePackage> changePackageById = new HashMap<String, MksChangePackage>();

	/**
	 * registers the changePackage as a changeList and start keeping the two of them in sync
	 *
	 * @param changePackage the changePackage to keep track of
	 * @return the changeList acting that is in sync with the changePackage
	 */
	@NotNull
	ChangeList trackMksChangePackage(@NotNull MksChangePackage changePackage) {
		ChangeListManager changeListManager = ChangeListManager.getInstance(mksVcs.getProject());
		if (changeListNameByChangePackageId.containsKey(changePackage.getId())) {
			changePackageById.put(changePackage.getId(), changePackage);

			LocalChangeList list = changeListManager.findChangeList(changeListNameByChangePackageId.get(changePackage.getId()));
			if (list != null) {
				return list;
			}
		}
		LocalChangeList changeList = changeListManager.findChangeList(createChangeListName(changePackage));
		if (changeList == null) {
			changeList = changeListManager.addChangeList(createChangeListName(changePackage), "");
		}
		changeListNameByChangePackageId.put(changePackage.getId(), changeList.getName());
		changePackageIdByChangeListName.put(changeList.getName(), changePackage.getId());
		changePackageById.put(changePackage.getId(), changePackage);
		return changeList;
	}

	private String createChangeListName(MksChangePackage changePackage) {
		return
//			"MKS CP " + changePackage.getId() + "," +
				changePackage.getSummary();
	}

	public boolean isChangeListMksControlled(@NotNull String changeListName) {
		return changePackageIdByChangeListName.containsKey(changeListName);
	}

	@Nullable
	public MksChangePackage getMksChangePackage(@NotNull String changeListName) {
		MksChangePackage mksChangePackage = changePackageById.get(changePackageIdByChangeListName.get(changeListName));
		if (mksChangePackage != null) {
			return mksChangePackage;
		} else {
			return null;
		}
	}

	@Override
	public void changeListRenamed(ChangeList list, String oldName) {
		if (isChangeListMksControlled(oldName)) {
			MksChangePackage mksChangePackage = getMksChangePackage(oldName);
			changeListNameByChangePackageId.remove(oldName);

			if (mksChangePackage != null) {
				changeListNameByChangePackageId.put(mksChangePackage.getId(), list.getName());
				changePackageIdByChangeListName.put(list.getName(), mksChangePackage.getId());
			}

			renameChangePackage(mksChangePackage, list.getName());
		}
	}

	private void renameChangePackage(MksChangePackage mksChangePackage, String name) {
		if (logger.isDebugEnabled()) {
//			logger.debug(
			logger.debug("renaming MKS CP [" + mksChangePackage.getId() + "] => \"" + name + "\"");
		}
		RenameChangePackage renameChangePackage = new RenameChangePackage(new ArrayList<VcsException>(), mksVcs, mksChangePackage, name);
		renameChangePackage.execute();
		if (renameChangePackage.foundError()) {
			logger.error("error renaming change package " + mksChangePackage);
		}
	}

	@Override
	public void changesMoved(Collection<Change> changes, ChangeList fromList, ChangeList toList) {
		// need to check the changes are controlled by mks
		if (isChangeListMksControlled(fromList.getName()) && isChangeListMksControlled(toList.getName()  /* todo changelist*/)) {
			// unlock then lock the changes
			// todo dispatch changes by sandbox
			// todo create and execute unlock command for each sandbox
			// todo create and execute lock command for each sandbox
//			new UnlockMemberCommand(new ArrayList<VcsException>(), mksVcs,
//					getMksChangePackage(fromList.getName()), ChangesUtil.getPaths(changes).to)
		} else if (isChangeListMksControlled(fromList.getName())) {
			// unlock the changes
			// todo dispatch changes by sandbox
			// todo create and execute unlock command for each sandbox
		} else if (isChangeListMksControlled(toList.getName())) {
			// lock the changes
			// todo dispatch changes by sandbox
			// todo create and execute lock command for each sandbox
		} else {
			super.changesMoved(changes, fromList, toList);
		}
		// todo update changePackages
	}


	@Override
	public void changeListRemoved(ChangeList list) {
		if (isChangeListMksControlled(list.getName())) {
			String cpId = changePackageIdByChangeListName.remove(list.getName());
			changeListNameByChangePackageId.remove(cpId);
			changePackageById.remove(cpId);
		}
	}
}
