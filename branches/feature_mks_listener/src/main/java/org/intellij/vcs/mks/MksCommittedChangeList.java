package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;

/**
 *
 */
public class MksCommittedChangeList implements CommittedChangeList {
	public Date getCommitDate() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getCommitterName() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public long getNumber() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public AbstractVcs getVcs() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public Collection<Change> getChanges() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getComment() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@NotNull
	public String getName() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
