package org.intellij.vcs.mks.realtime;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.sicommands.ListSandboxes;

/**
 * @author Thibaut Fagart
 */
public class SandboxListSynchronizer extends AbstractMKSSynchronizer {
	private static final String LINE_SEPARATOR = " -> ";
	// $sandbox$ -> $project$[$sandboxtype$:$projectVersionOrDevPath$] ($server$:$port$)
	private static final String patternString = "(.+) -> ([^\\[]+)(?:\\[([^:]+):([^:]+)\\])? \\((.+)\\)";
	private final SandboxCache sandboxCache;
	private final Pattern pattern;
	private static final int SANDBOX_PATH_GROUP_IDX = 1;
	private static final int PROJECT_PATH_GROUP_IDX = 2;
	private static final int PROJECT_TYPE_GROUP_IDX = 3;
	private static final int PROJECT_VERSION_GROUP_IDX = 4;
	private static final int SERVER_GROUP_IDX = 5;

	public SandboxListSynchronizer(EncodingProvider encodingProvider, SandboxCache sandboxCache) {
		super(ListSandboxes.COMMAND, encodingProvider/*, "--displaySubs"*/);
		this.sandboxCache = sandboxCache;
		pattern = Pattern.compile(patternString);
	}

	@Override
	protected void handleLine(String line) {
		try {
			if (shoudIgnore(line)) return;
			if (line.startsWith("-----")) {
				// detection of a new update
				LOGGER.debug("update notification : "+line);
                sandboxCache.clear();
			} else {
				Matcher matcher = pattern.matcher(line);
//				String[] parts = line.split(LINE_SEPARATOR);
				if (!matcher.matches()) {
					LOGGER.error("unexpected command output {" + line + "}, expected something matching "+ patternString, "");
					// ignoring line
				} else {
					String sandboxPath = matcher.group(SANDBOX_PATH_GROUP_IDX);
					String projectPath = matcher.group(PROJECT_PATH_GROUP_IDX);
					String projectType = matcher.group(PROJECT_TYPE_GROUP_IDX);
					String projectVersion = matcher.group(PROJECT_VERSION_GROUP_IDX);
					String serverHostAndPort = matcher.group(SERVER_GROUP_IDX);
//					System.out.println("adding ["+filePath+"]");
					sandboxCache.addSandboxPath(sandboxPath, serverHostAndPort, projectPath, projectVersion);
				}
			}
		} catch (Exception e) {
			LOGGER.error("error parsing mks synchronizer output ["+line+"], skipping that line",e);
		}
	}

	public String getDescription() {
		return "sandbox list listener";
	}
}
