package org.intellij.vcs.mks.realtime;

import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.sicommands.ListChangePackages;

/**
 * @author Thibaut Fagart
 */
public class ChangePackageSynchronizer extends AbstractMKSSynchronizer {
	private final ChangePackageCache changePackageCache;
    private final String server;

    public ChangePackageSynchronizer(EncodingProvider encodingProvider, ChangePackageCache changePackageCache, String server) {
		super(ListChangePackages.COMMAND, encodingProvider, (server==null)?new String[]{ListChangePackages.ARGS}:new String[]{ListChangePackages.ARGS,"--hostname",server});
        this.server = server;
		this.changePackageCache = changePackageCache;
	}

	@Override
	protected void handleLine(String line) {
		try {
			if (shoudIgnore(line)) return;
			if (line.startsWith("-----")) {
				// detection of a new update
				LOGGER.debug("update notification : " + line);
				changePackageCache.clear();
			} else {

				String[] parts = line.split("\t");
				if (parts.length < 4) {
					LOGGER.error("unexepcted command output {" + line + "}, expected 4 parts separated by \\t", "");
				} else {
					changePackageCache.addChangePackage(new MksChangePackage(server, parts[ListChangePackages.ID], parts[ListChangePackages.USER], parts[ListChangePackages.STATE], parts[ListChangePackages.SUMMARY]));
				}
			}
		} catch (Exception e) {
			LOGGER.error("error parsing mks synchronizer output [" + line + "], skipping that line", e);
		}
	}

	public String getDescription() {
		return "change package list listener for " + this.server;
	}
}