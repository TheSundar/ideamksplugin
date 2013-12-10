package org.intellij.vcs.mks.realtime;

import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thibaut Fagart
 */
public class SandboxListSynchronizerImplTest extends TestCase {
	private SandboxListSynchronizerImpl synchro;

	protected void setUp() throws Exception {
		super.setUp();
		synchro = new SandboxListSynchronizerImpl(null);
	}

	public void test() throws IOException {
		final InputStream stream = getClass().getResourceAsStream("sandboxlist.properties");
		final InputStreamReader reader = new InputStreamReader(stream);
		final BufferedReader bufferedReader = new BufferedReader(reader);
		String line;

		final List<MksSandboxInfo> sandboxes = new ArrayList<MksSandboxInfo>();
		synchro.addListener(new SandboxListListener() {
			public void clear() {
				sandboxes.clear();
			}

			public void addSandboxPath(@NotNull String sandboxPath, @NotNull String serverHostAndPort,
									   @NotNull String mksProject, @Nullable String devPath, boolean isSubSandbox) {
				final MksSandboxInfo sandboxInfo = new MksSandboxInfo(sandboxPath, serverHostAndPort, mksProject, devPath, null, isSubSandbox);
				sandboxes.add(sandboxInfo);
			}
		});

		while ((line = bufferedReader.readLine()) != null) {
			synchro.handleLine(line);
		}

		assertFalse(sandboxes.get(0).isSubSandbox);
		for (MksSandboxInfo sandboxInfo : sandboxes.subList(1, sandboxes.size())) {
			assertTrue(sandboxInfo.isSubSandbox);
		}
		assertEquals(12, sandboxes.size());

	}
}
