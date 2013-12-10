package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Thibaut Fagart
 */
public class ViewSandboxLocalChangesOrLockedCommandTest extends TestCase {
	private static final String ENCODING = "IBM437";
	private ViewSandboxLocalChangesOrLockedCommand commandLocal;
	private List<VcsException> errors;
	@NonNls
	private static final String SANDBOX_DIR = "c:\\Documents and Settings\\A6253567\\sandboxes\\J2EE";

	protected void setUp() throws Exception {
		super.setUp();
		errors = new ArrayList<VcsException>();

	}

	public void testFindsModifiedWithoutCheckout() {
		String sandboxPath = SANDBOX_DIR + "\\project.pj";
		commandLocal = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		commandLocal.execute();
		String modifiedWithoutCheckOutFile = SANDBOX_DIR + "\\HJF-Core\\unittestsrc\\log4j.properties";
		MksMemberState memberState = commandLocal.getMemberStates().get(modifiedWithoutCheckOutFile);
		assertNotNull("missing state", memberState);
		assertTrue("bad modifiedWithoutCheckout status", memberState.status == MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);
		assertFalse("bad checkout status", memberState.status == MksMemberState.Status.CHECKED_OUT);

	}

	public void testFindsCheckedOutWithChangePackage() {
		String sandboxPath = SANDBOX_DIR + "\\project.pj";
		commandLocal = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		commandLocal.execute();
		String checkedOutFile = SANDBOX_DIR + "\\HJE-Batch\\pom.xml";

		MksMemberState memberState = commandLocal.getMemberStates().get(checkedOutFile);
		assertNotNull("missing state", memberState);
		assertNotNull("missing CP id", memberState.workingChangePackageId);
		assertEquals("bad CP id", "2951:1", memberState.workingChangePackageId);
		assertEquals("bad checkedout status", MksMemberState.Status.CHECKED_OUT, memberState.status);
	}

	public void testViewsCheckedOutInAnotherSandboxBySameUserAsUnknown() {
		String sandboxPath = SANDBOX_DIR + "\\project.pj";
		commandLocal = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		commandLocal.execute();
		String checkedOutInAnotherSandboxFile = SANDBOX_DIR + "\\HJE-Batch\\src\\com\\hsbc\\hbfr\\ccf\\at\\batch\\Batch.java";

		MksMemberState memberState = commandLocal.getMemberStates().get(checkedOutInAnotherSandboxFile);
		assertNotNull("missing state", memberState);
		assertEquals("expecting UNKNOWN", MksMemberState.Status.UNKNOWN, memberState.status);
	}

	public void testFile() throws IOException {
		System.out.println(new File("c:/Readme.txt").getCanonicalPath());
	}

	public void testLocallyDeleted() {
		String sandboxPath = SANDBOX_DIR + "\\project.pj";
		commandLocal = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		commandLocal.execute();
		String locallyDeletedFile = SANDBOX_DIR + "\\HJF-Core\\build\\HJF-Core-debug.jar";

		MksMemberState memberState = commandLocal.getMemberStates().get(locallyDeletedFile);
		assertNotNull("missing state", memberState);
		assertFalse("bad checkedout status", memberState.status == MksMemberState.Status.CHECKED_OUT);
		assertTrue("bad modifiedWithoutCheckout status", memberState.status == MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);

	}

	public void testDeferredAdd() {
		List<VcsException> errors = new ArrayList<VcsException>();

		String sandboxPath = SANDBOX_DIR + "\\sandboxes\\CSO\\project.pj";
		commandLocal = createCommand(errors, sandboxPath, "viewsandbox/deferred-add.txt");
		commandLocal.execute();
		Map<String, MksMemberState> states = commandLocal.getMemberStates();
		assertEquals(38, states.size());
		for (Map.Entry<String, MksMemberState> entry : states.entrySet()) {
			assertEquals(MksMemberState.Status.ADDED, entry.getValue().status);
			assertNotNull(entry.getValue().workingChangePackageId);
			assertNull(entry.getValue().memberRevision);
		}
	}

	public void testDeferredDrop() {
		List<VcsException> errors = new ArrayList<VcsException>();

		String sandboxPath = SANDBOX_DIR + "\\sandboxes\\CSO\\project.pj";
		commandLocal = createCommand(errors, sandboxPath, "viewsandbox/deferred-drop.txt");
		commandLocal.execute();
		Map<String, MksMemberState> states = commandLocal.getMemberStates();
		assertEquals(5, states.size());
		for (Map.Entry<String, MksMemberState> entry : states.entrySet()) {
			assertEquals(MksMemberState.Status.DROPPED, entry.getValue().status);
			assertNotNull(entry.getValue().workingChangePackageId);
			assertNull(entry.getValue().workingRevision);
			assertNotNull(entry.getValue().memberRevision);
		}
	}

	private ViewSandboxLocalChangesOrLockedCommand createCommand(final List<VcsException> errors, final String sandboxPath, final String outputFile) {
		EncodingProvider encodingProvider = new EncodingProvider() {
			@NotNull
			public String getMksSiEncoding(final String command) {
				return ENCODING;
			}
		};
		return new ViewSandboxLocalChangesOrLockedCommand(errors, encodingProvider, "e9310750", sandboxPath) {
			@Override
			protected String executeCommand() throws IOException {
				commandOutput = loadResourceWithEncoding(outputFile, ENCODING);
				return outputFile;
			}

			protected void setState(@NotNull String name, @NotNull MksMemberState memberState) {
				memberStates.put(name, memberState);
			}

		};
	}

	private String loadResourceWithEncoding(final String path, final String encoding) throws IOException {
		URL resource = getClass().getResource(path);
		InputStream inputStream = resource.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String line;
		while ((line = reader.readLine()) != null) {
			pw.println(line);
		}
		pw.flush();
		return sw.toString();
	}

}