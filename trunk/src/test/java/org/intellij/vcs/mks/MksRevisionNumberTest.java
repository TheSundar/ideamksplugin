package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;

public class MksRevisionNumberTest extends TestCase {

	public void testRootRevision() throws VcsException {
		MksRevisionNumber rev = new MksRevisionNumber("1.1");
		assertEquals("", rev.getParentRevision());
	}

	public void testMainBranchRevision() throws VcsException {
		MksRevisionNumber rev = new MksRevisionNumber("1.5");
		assertEquals("", rev.getParentRevision());
	}

	public void testBranchingFromRootRevision() throws VcsException {
		MksRevisionNumber rev = new MksRevisionNumber("1.2.1.1");
		assertEquals("1.2", rev.getParentRevision());
	}

	public void testSecondBranchFromRootRevision() throws VcsException {
		MksRevisionNumber rev = new MksRevisionNumber("1.2.2.1");
		assertEquals("1.2", rev.getParentRevision());
	}

	public void testNormalInBranch() throws VcsException {
		MksRevisionNumber rev = new MksRevisionNumber("1.2.2.3");
		assertEquals("1.2", rev.getParentRevision());
	}

	public void testBranchFromBranch() throws VcsException {
		MksRevisionNumber rev = new MksRevisionNumber("1.2.2.3.1.1");
		assertEquals("1.2.2.3", rev.getParentRevision());

	}
}
