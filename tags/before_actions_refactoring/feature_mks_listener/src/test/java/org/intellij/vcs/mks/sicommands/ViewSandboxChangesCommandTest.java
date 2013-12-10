package org.intellij.vcs.mks.sicommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksMemberState;
import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;

/**
 * @author Thibaut Fagart
 */
public class ViewSandboxChangesCommandTest extends TestCase {
	private static final String ENCODING = "IBM437";
	private ViewSandboxLocalChangesCommand command;
	private List<VcsException> errors;

	protected void setUp() throws Exception {
		super.setUp();
		errors = new ArrayList<VcsException>();

	}

	public void testModifiedWithoutCheckout() {
		String sandboxPath = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\project.pj";
		command = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		command.execute();
		String modifiedWithoutCheckOutFile = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\unittestsrc\\log4j.properties";
		MksMemberState memberState = command.getMemberStates().get(modifiedWithoutCheckOutFile);
		assertNotNull("missing state", memberState);
		assertTrue("bad modifiedWithoutCheckout status", memberState.status== MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);
		assertFalse("bad checkout status", memberState.status == MksMemberState.Status.CHECKED_OUT);

	}

	public void testCheckedOutWithChangePackage() {
		String sandboxPath = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\project.pj";
		command = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		command.execute();
		String checkedOutFile = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\unittestsrc\\com\\hsbc\\hbfr\\ccf\\at\\util\\PerfLogUtilsTestCase.java";

		MksMemberState memberState = command.getMemberStates().get(checkedOutFile);
		assertNotNull("missing state", memberState);
		assertNotNull("missing CP id", memberState.workingChangePackageId);
		assertEquals("bad CP id","2875:1", memberState.workingChangePackageId);
		assertTrue("bad checkedout status", memberState.status== MksMemberState.Status.CHECKED_OUT);
		assertFalse("bad modifiedWithoutCheckout status", memberState.status== MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);

	}

	public void testCheckedOutInAnotherSandboxBySameUser() {
		String sandboxPath = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\project.pj";
		command = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		command.execute();
		String checkedOutInAnotherSandboxFile = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\pom.xml";

		MksMemberState memberState = command.getMemberStates().get(checkedOutInAnotherSandboxFile);
		assertNotNull("missing state", memberState);
		assertFalse("bad checkedout status", memberState.status== MksMemberState.Status.CHECKED_OUT);
		assertFalse("bad modifiedWithoutCheckout status", memberState.status== MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);
	}

	public void testLocallyDeleted() {
		String sandboxPath = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\project.pj";
		command = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		command.execute();
		String locallyDeletedFile = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\build\\HJF-Core-debug.jar";

		MksMemberState memberState = command.getMemberStates().get(locallyDeletedFile);
		assertNotNull("missing state", memberState);
		assertFalse("bad checkedout status", memberState.status== MksMemberState.Status.CHECKED_OUT);
		assertTrue("bad modifiedWithoutCheckout status", memberState.status== MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);

	}
	public void testLocallyModifiedAndCheckedOut() {
		String sandboxPath = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\project.pj";
		command = createCommand(errors, sandboxPath, "viewsandbox/changedOrLocked.txt");
		command.execute();
//        List < VcsException > errors = new ArrayList<VcsException>();
//
//        String sandboxPath = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\project.pj";
//        ViewSandboxLocalChangesCommand command = createCommand(errors, sandboxPath);
//        command.execute();

		Map<String, MksMemberState> states = command.getMemberStates();
		for (Map.Entry<String, MksMemberState> entry : states.entrySet()) {
			MksMemberState state = entry.getValue();
			System.out.println(entry.getKey() + " => "+state);
			assertNotNull("no member revision", state.memberRevision);
			assertNotNull("no working revision", state.workingRevision);
		}
		boolean error = command.foundError();
		if (error) {
			for (VcsException vcsException : errors) {
				System.err.println("erreur "+vcsException);
			}
		}
		assertFalse("errors found", error);
	}

	public void testDeferredAdd() {
		List<VcsException> errors = new ArrayList<VcsException>();

		String sandboxPath = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\DS2\\Presentation\\esf-servlet-rp\\project.pj";
		command = createCommand(errors, sandboxPath, "viewsandbox/deferred-add.txt");
		command.execute();
		Map<String, MksMemberState> states = command.getMemberStates();
		assertEquals(1, states.size());
		MksMemberState state = states.values().iterator().next();
		assertEquals(MksMemberState.Status.ADDED, state.status);
	}
	private ViewSandboxLocalChangesCommand createCommand(final List<VcsException> errors, final String sandboxPath, final String outputFile) {
		EncodingProvider encodingProvider = new EncodingProvider() {
			public String getMksSiEncoding(final String command) {
				return ENCODING;
			}
		};
		return new ViewSandboxLocalChangesCommand(errors, encodingProvider, "e9310750", sandboxPath) {
			@Override
			protected String executeCommand() throws IOException {
				commandOutput = loadResourceWithEncoding(outputFile, ENCODING);
				return outputFile;
			}
		};
	}
	private String loadResourceWithEncoding(final String path, final String encoding) throws IOException {
		URL resource = getClass().getResource(path);
		InputStream inputStream = resource.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String line = null;
		while ((line = reader.readLine()) != null) {
			pw.println(line);
		}
		pw.flush();
		return sw.toString();
	}

}