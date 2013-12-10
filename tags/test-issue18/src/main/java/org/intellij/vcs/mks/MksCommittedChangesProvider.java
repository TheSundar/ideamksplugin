package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.versionBrowser.ChangesBrowserSettingsEditor;

import java.util.List;

/**
 * The CachingCommittedChangesProvider is polled by timer after the user has
 * initialized the VCS history cache, regardless of whether the tab is active.
 *
 * @see http://www.intellij.net/forums/thread.jspa?messageID=5194739&#5194739
 */
public class MksCommittedChangesProvider implements CommittedChangesProvider<MksCommittedChangeList, MksChangeBrowserSettings> {
	private final MksVcs mksVcs;

	public MksCommittedChangesProvider(MksVcs mksVcs) {
		this.mksVcs = mksVcs;
	}

	public MksChangeBrowserSettings createDefaultSettings() {
		return null;
	}

	public ChangesBrowserSettingsEditor createFilterUI(boolean showDateFilter) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public ChangeListColumn[] getColumns() {
		return new ChangeListColumn[0];  //To change body of implemented methods use File | Settings | File Templates.
	}

	public RepositoryLocation getLocationFor(FilePath root) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public List<MksCommittedChangeList> getCommittedChanges(MksChangeBrowserSettings settings, RepositoryLocation location, int maxCount) throws VcsException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
