package org.intellij.vcs.mks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.sicommands.LockMemberCommand;
import org.intellij.vcs.mks.sicommands.RenameChangePackage;
import org.intellij.vcs.mks.sicommands.UnlockMemberCommand;
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
	private boolean isUpdating = false;
	private final Map<String, String> changeListNameByChangePackageId = new HashMap<String, String>();
	private final Map<String, MksChangePackage> changePackageById = new HashMap<String, MksChangePackage>();
	private final Map<String, String> changePackageIdByChangeListName = new HashMap<String, String>();

	public MksChangeListAdapter(@NotNull MksVcs mksVcs) {
		this.mksVcs = mksVcs;
	}

	void setUpdating(boolean b) {
		isUpdating = b;
	}

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

			LocalChangeList list =
					changeListManager.findChangeList(changeListNameByChangePackageId.get(changePackage.getId()));
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
			if (!isUpdating) {
				renameChangePackage(mksChangePackage, list.getName());
			}
		}
	}

	private void renameChangePackage(MksChangePackage mksChangePackage, String name) {
		if (logger.isDebugEnabled()) {
//			logger.debug(
			logger.debug("renaming MKS CP [" + mksChangePackage.getId() + "] => \"" + name + "\"");
		}
		RenameChangePackage renameChangePackage =
				new RenameChangePackage(new ArrayList<VcsException>(), mksVcs, mksChangePackage, name);
		renameChangePackage.execute();
		if (renameChangePackage.foundError()) {
			logger.error("error renaming change package " + mksChangePackage);
		}
	}

	/**
	 * non vcs controlled members moved to a mks changepackage should be added
	 *
	 * @param changes
	 * @param fromList
	 * @param toList
	 */
	@Override
	public void changesMoved(Collection<Change> changes, ChangeList fromList, ChangeList toList) {
		if (isUpdating) {
			return;
		}
		if (true) {
			// todo remove when change moving is definitely supported
			return;
		}
		if (isChangeListMksControlled(fromList.getName()) || isChangeListMksControlled(toList.getName())) {

			Map<MksSandboxInfo, ArrayList<VirtualFile>> filesBysandbox = dispatchBySandbox(changes);
			// need to check the changes are controlled by mks
			if (isChangeListMksControlled(fromList.getName()) &&
					isChangeListMksControlled(toList.getName()  /* todo changelist*/)) {
				// unlock then lock the changes
				final MksChangePackage aPackage = getMksChangePackage(toList.getName());
				if (aPackage == null) {
					logger.warn("unable to find the change package for [" + toList.getName() + "]");
					return;
				}
				for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : filesBysandbox.entrySet()) {
					final String[] paths = getPaths(entry);
					unlock(entry.getKey(), paths);
					lock(entry.getKey(), aPackage, paths);
				}
			} else if (isChangeListMksControlled(fromList.getName())) {
				// unlock the changes
				// todo
				// changes for which beforeRevision == null are newly created, and if they appear in a controlled list
				// they have been added (defeferred)
				// changes for which beforeRevision != null were regularly checked out and should be unlocked
				final MksChangePackage aPackage = getMksChangePackage(fromList.getName());
				if (aPackage == null) {
					logger.warn("unable to find the change package for [" + fromList.getName() + "]");
					return;
				}
				for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : filesBysandbox.entrySet()) {
					final String[] paths = getPaths(entry);
					removeDeferred(entry.getKey(), aPackage, paths);
				}
			} else if (isChangeListMksControlled(toList.getName())) {
				// lock the changes
				// if change.beforeRevision == null , this is a new file, and should be added (deferred= true)
				// if change.beforeRevision != null ... shouldn't be possible
				final MksChangePackage aPackage = getMksChangePackage(toList.getName());
				if (aPackage == null) {
					logger.warn("unable to find the change package for [" + toList.getName() + "]");
					return;
				}
				for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : filesBysandbox.entrySet()) {
					final String[] paths = getPaths(entry);
//					lock(entry.getKey(), aPackage, paths);
					addDeferred(entry.getKey(), aPackage, paths);
				}
			}
		} else {
			super.changesMoved(changes, fromList, toList);
		}
		if (isChangeListMksControlled(fromList.getName()) != isChangeListMksControlled(toList.getName())) {
			for (Change change : changes) {
				final ContentRevision afterRevision = change.getAfterRevision();
				final ContentRevision beforeRevision = change.getBeforeRevision();
				if (afterRevision != null) {
					logger.warn("dirtying file " + afterRevision.getFile());
					VcsDirtyScopeManager.getInstance(mksVcs.getProject()).fileDirty(afterRevision.getFile());
				}
				if (beforeRevision != null) {
					logger.warn("dirtying file " + beforeRevision.getFile());
					VcsDirtyScopeManager.getInstance(mksVcs.getProject()).fileDirty(beforeRevision.getFile());
				}
			}
		}
	}

	private void addDeferred(MksSandboxInfo sandboxInfo, MksChangePackage aPackage, String[] paths) {
		throw new UnsupportedOperationException("TODO : not yet implemented");
	}

	private void removeDeferred(MksSandboxInfo sandboxInfo, MksChangePackage aPackage, String[] paths) {
		throw new UnsupportedOperationException("TODO : not yet implemented");
	}

	private String[] getPaths(Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry) {
		final ArrayList<VirtualFile> files = entry.getValue();
		final String[] paths = new String[files.size()];
		for (int i = 0, max = paths.length; i < max; i++) {
			paths[i] = files.get(i).getPath();
		}
		return paths;
	}

	private Map<MksSandboxInfo, ArrayList<VirtualFile>> dispatchBySandbox(Collection<Change> changes) {
		DispatchBySandboxCommand dispatchAction = new DispatchBySandboxCommand(mksVcs,
				ChangesUtil.getFilesFromChanges(changes));
		dispatchAction.execute();

		Map<MksSandboxInfo, ArrayList<VirtualFile>> filesBysandbox = dispatchAction.getFilesBySandbox();
		return filesBysandbox;
	}

	private void lock(MksSandboxInfo sandbox, MksChangePackage aPackage, String[] pathsToUnlock) {
		final LockMemberCommand lockCmd = new LockMemberCommand(new ArrayList<VcsException>(), mksVcs, sandbox,
				aPackage, pathsToUnlock);
		lockCmd.execute();
	}

	private void unlock(MksSandboxInfo sandbox, String[] pathsToUnlock) {
		final UnlockMemberCommand unlockCmd =
				new UnlockMemberCommand(new ArrayList<VcsException>(), mksVcs, sandbox, pathsToUnlock);
		unlockCmd.execute();
	}

	/**
	 * Lookup is done using the cpid, not the cp name.
	 * This allows looking up a changelist even the change package has been renamed
	 *
	 * @param cp
	 * @return the changelist mapped to the given change package if any
	 */
	@Nullable
	public ChangeList getChangeList(@NotNull MksChangePackage cp) {
		final String changeListName = this.changeListNameByChangePackageId.get(cp.getId());
		if (changeListName == null) {
			return null;
		} else {
			return ChangeListManager.getInstance(mksVcs.getProject()).findChangeList(changeListName);
		}
	}

	@Override
	public void changeListRemoved(ChangeList list) {
		if (!isUpdating && isChangeListMksControlled(list.getName())) {
			String cpId = changePackageIdByChangeListName.remove(list.getName());
			changeListNameByChangePackageId.remove(cpId);
			changePackageById.remove(cpId);
		}
	}
}
