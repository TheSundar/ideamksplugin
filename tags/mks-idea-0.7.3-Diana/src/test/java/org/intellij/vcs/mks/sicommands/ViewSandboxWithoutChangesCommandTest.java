package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;
import org.intellij.vcs.mks.CommandExecutionListener;
import org.intellij.vcs.mks.MksCLIConfiguration;
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
			assertNotNull("no member revision", state.memberRevision);
			assertNotNull("no working revision", state.workingRevision);
			assertEquals("should only see non changed files", MksMemberState.Status.NOT_CHANGED, state.status);
			assertNull("should not see working cpid, as only non changed files are fetched",
					state.workingChangePackageId);
		}
		for (VcsException error : errors) {
			System.err.println("error " + error);
			error.printStackTrace();
		}
		assertTrue("errors found", errors.isEmpty());
		String member =
				"c:\\Documents and Settings\\A6253567\\sandboxes\\J2EE\\HJF-Core\\pom.xml";
		// this command is not supposed to detect changes
		MksMemberState state = states.get(member);
		assertNotNull(state);
		assertEquals("1.4.1.1", state.workingRevision.asString());
		assertEquals("1.4.1.1", state.memberRevision.asString());
	}

	private ViewSandboxWithoutChangesCommand createCommand(final List<VcsException> errors, final String sandboxPath) {
		MksCLIConfiguration mksCLIConfiguration = new MksCLIConfiguration() {
			@NotNull
			public String getMksSiEncoding(final String command) {
				return ENCODING;
			}

			@NotNull
			public String getDatePattern() {
				return "MMM dd, yyyy - hh:mm a";
			}

			public CommandExecutionListener getCommandExecutionListener() {
				return CommandExecutionListener.IDLE;
			}
		};
		return new ViewSandboxWithoutChangesCommand(errors, mksCLIConfiguration, sandboxPath) {
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
		String line;
		while ((line = reader.readLine()) != null) {
			pw.println(line);
		}
		pw.flush();
		return sw.toString();
	}

	public void testEncoding() throws IOException {
		for (String encoding : Charset.availableCharsets().keySet()) {
			if (testFileUsing("viewsandbox/sample1.txt", encoding, "Working file 1,082 bytes larger")) {
				System.out.println("encoding " + encoding + " OK");
			}
		}

	}

	private boolean testFileUsing(final String resourceName, final String encoding, final String expected) throws
			IOException {
		URL testFile = getClass().getResource(resourceName);
		InputStream inputStream = testFile.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
		try {
			String line;
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
				e.printStackTrace();
			}
		}

	}
}
