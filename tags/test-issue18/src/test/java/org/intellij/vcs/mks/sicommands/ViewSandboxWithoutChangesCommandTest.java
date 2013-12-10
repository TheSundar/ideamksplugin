package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksMemberState;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Thibaut Fagart
 */
public class ViewSandboxWithoutChangesCommandTest extends TestCase {
	private static final String ENCODING = "IBM437";

	public void testSimple() {
		List<VcsException> errors = new ArrayList<VcsException>();

		String sandboxPath = "c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\project.pj";
		ViewSandboxWithoutChangesCommand command = createCommand(errors, sandboxPath);
		command.execute();
		Map<String, MksMemberState> states = command.getMemberStates();
		for (Map.Entry<String, MksMemberState> entry : states.entrySet()) {
			MksMemberState state = entry.getValue();
			assertFalse("there is no modifiedWithoutCheckout files", state.status == MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT);
			assertNotNull("no member revision", state.memberRevision);
			assertNotNull("no working revision", state.workingRevision);
		}
		assertTrue("errors found", errors.isEmpty());
		String modifiedWithoutCheckoutFile =
				"c:\\Documents and Settings\\A6253567.HBEU\\sandboxes\\J2EE\\HJF-Core\\unittestsrc\\com\\hsbc\\hbfr\\ccf\\at\\util\\PerfLogUtilsTestCase.java";
		// this command is not supposed to detect changes
		MksMemberState memberState = states.get(modifiedWithoutCheckoutFile);
		assertFalse(memberState.status == MksMemberState.Status.CHECKED_OUT);
		assertNotNull(states.get(modifiedWithoutCheckoutFile).workingChangePackageId);
		assertEquals("2875:1", states.get(modifiedWithoutCheckoutFile).workingChangePackageId);
	}

	private ViewSandboxWithoutChangesCommand createCommand(final List<VcsException> errors, final String sandboxPath) {
		EncodingProvider encodingProvider = new EncodingProvider() {
			public String getMksSiEncoding(final String command) {
				return ENCODING;
			}
		};
		return new ViewSandboxWithoutChangesCommand(errors, encodingProvider, sandboxPath) {
			@Override
			protected String executeCommand() throws IOException {
				commandOutput = loadResourceWithEncoding("viewsandbox/sample1.txt", ENCODING);
				return "viewsandbox/sample1.txt";
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
		String line = null;
		while ((line = reader.readLine()) != null) {
			pw.println(line);
		}
		pw.flush();
		return sw.toString();
	}

	public void testEncoding() throws IOException {
		for (String encoding : Charset.availableCharsets().keySet()) {
			if (testFileUsing("viewsandbox/sample1.txt", encoding, "Working file 1 082 bytes larger")) {
				System.out.println("encoding " + encoding + " OK");
			}
		}

	}

	private boolean testFileUsing(final String resourceName, final String encoding, final String expected) throws IOException {
		URL testFile = getClass().getResource(resourceName);
		InputStream inputStream = testFile.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
		try {
			String line = null;
			boolean found = false;
			int i = 1;
			while (!found && (line = reader.readLine()) != null) {
				found = line.contains(expected);
				if (found) {
					System.out.println("line " + i + " contains [" + expected + "]");
				}
				i++;
			}
			return found;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}

	}
}
