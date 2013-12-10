package org.intellij.vcs.mks.realtime;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.intellij.vcs.mks.MksConfiguration;
import org.intellij.vcs.mks.sicommands.ListSandboxes;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thibaut Fagart
 */
public class SandboxListSynchronizerImpl extends AbstractMKSSynchronizer implements ApplicationComponent, SandboxListSynchronizer {
	//	private static final String LINE_SEPARATOR = " -> ";
	// $sandbox$ -> $project$[$sandboxtype$:$projectVersionOrDevPath$] ($server$:$port$)
	private static final String patternString = "(.+) -> ([^\\[]+)(?:\\[([^:]+):([^:]+)\\])? \\((.+)\\)";
	private final ArrayList<SandboxListListener> listeners = new ArrayList<SandboxListListener>();
	private final Pattern pattern;
	private static final int SANDBOX_PATH_GROUP_IDX = 1;
	private static final int PROJECT_PATH_GROUP_IDX = 2;
	private static final int PROJECT_TYPE_GROUP_IDX = 3;
	private static final int PROJECT_VERSION_GROUP_IDX = 4;
	private static final int SERVER_GROUP_IDX = 5;

	private SandboxInfo currentTopSandbox = null;

	public SandboxListSynchronizerImpl() {
		super(ListSandboxes.COMMAND, ApplicationManager.getApplication().getComponent(MksConfiguration.class), "--displaySubs");
		pattern = Pattern.compile(patternString);
	}

	public void addListener(@NotNull SandboxListListener listener) {
		if (this.listeners.contains(listener)) {
			return;
		}
		this.listeners.add(listener);
		for (SandboxInfo sandbox : currentBatch) {
			listener.addSandboxPath(sandbox.sandboxPath, sandbox.serverHostAndPort, sandbox.projectPath, sandbox.projectVersion, sandbox.subSandbox);
		}
	}

	public void removeListener(@NotNull SandboxListListener sandboxListListener) {
		this.listeners.remove(sandboxListListener);
	}


	@NonNls
	@NotNull
	public String getComponentName() {
		return "MKS.sandboxListSaynchronizer";
	}

	public void initComponent() {
		start();
	}

	public void disposeComponent() {
		stop();
	}

	private final ArrayList<SandboxInfo> currentBatch = new ArrayList<SandboxInfo>();

	@Override
	protected void handleLine(String line) {
		try {
			if (shoudIgnore(line)) {
				return;
			}
			if (line.startsWith("-----")) {
				// detection of a new update
				LOGGER.debug("update notification : " + line);
				fireSandboxReset();
			} else {

				Matcher matcher = pattern.matcher(line);
//				String[] parts = line.split(LINE_SEPARATOR);
				if (!matcher.matches()) {
					LOGGER.error("unexpected command output {" + line + "}, expected something matching " + patternString, "");
					// ignoring line
				} else {
					String sandboxPath = matcher.group(SANDBOX_PATH_GROUP_IDX);
					String projectPath = matcher.group(PROJECT_PATH_GROUP_IDX);
					String projectType = matcher.group(PROJECT_TYPE_GROUP_IDX);
					String projectVersion = matcher.group(PROJECT_VERSION_GROUP_IDX);
					String serverHostAndPort = matcher.group(SERVER_GROUP_IDX);
					final SandboxInfo info = resolveSandbox(sandboxPath, serverHostAndPort, projectPath, projectVersion);
					fireSandboxAdded(info);
				}
			}
		} catch (Exception e) {
			LOGGER.error("error parsing mks synchronizer output [" + line + "], skipping that line  because : " + e.getMessage(), e);
		}
	}

	private SandboxInfo resolveSandbox(String sandboxPath, String serverHostAndPort, String projectPath, String projectVersion) {
		boolean isSubSandbox = isSubSandbox(projectPath);
		SandboxInfo info;
		if (isSubSandbox) {
			if (currentTopSandbox == null) {
				throw new IllegalStateException("encountering a subsandbox without its containing sandbox");
			}
			info = new SandboxInfo(currentTopSandbox, sandboxPath, projectPath, projectVersion);
		} else {
			if (projectPath.indexOf('/') < 0) {
				throw new IllegalStateException("projectPath [" + projectPath + "] does not contain /");
			}
			info = new SandboxInfo(sandboxPath, serverHostAndPort, projectPath, projectVersion);
			currentTopSandbox = info;
		}
		return info;
	}

	private static final class SandboxInfo {
		private final String sandboxPath;
		private final String projectPath;
		private final String projectVersion;
		private final String serverHostAndPort;
		private final boolean subSandbox;

		SandboxInfo(String sandboxPath, String serverHostAndPort, String projectPath, String projectVersion) {
			this.sandboxPath = sandboxPath;
			this.serverHostAndPort = serverHostAndPort;
			this.projectPath = projectPath;
			this.projectVersion = projectVersion;
			this.subSandbox = false;

		}

		public SandboxInfo(SandboxInfo parentSandbox, String sandboxPath, String subProjectPath, String projectVersion) {
			this.sandboxPath = sandboxPath;
			this.projectPath = parentSandbox.getServerFolder() + subProjectPath;
			this.serverHostAndPort = parentSandbox.serverHostAndPort;
			this.projectVersion = projectVersion;
			this.subSandbox = true;
		}

		public String getServerFolder() {
			return projectPath.substring(0, projectPath.lastIndexOf('/') + 1);
		}
	}

	private void fireSandboxAdded(SandboxInfo sandbox) {
		currentBatch.add(sandbox);
		for (SandboxListListener listener : listeners) {
			listener.addSandboxPath(sandbox.sandboxPath, sandbox.serverHostAndPort, sandbox.projectPath, sandbox.projectVersion, sandbox.subSandbox);
		}
	}

	private void fireSandboxReset() {
		currentBatch.clear();
		for (SandboxListListener listener : listeners) {
			listener.clear();
		}
	}

	private boolean isSubSandbox(String projectPath) {
		return !(projectPath.charAt(0) == '/');
	}

	public String getDescription() {
		return "sandbox list listener";
	}
}
