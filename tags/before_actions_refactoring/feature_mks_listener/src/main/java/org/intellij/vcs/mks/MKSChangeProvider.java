package org.intellij.vcs.mks;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Processor;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.model.MksMemberState;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.realtime.SandboxCache;
import org.intellij.vcs.mks.sicommands.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Thibaut Fagart
 * @see com.intellij.openapi.vcs.changes.VcsDirtyScopeManager allows to notify
 *      idea if files should be marked dirty
 */
class MKSChangeProvider extends AbstractProjectComponent implements ChangeProvider, ProjectComponent, ChangeListDecorator {
	private final Logger logger = Logger.getInstance(getClass().getName());
	private final Logger BUILDER_PROXY_LOGGER = Logger.getInstance(getClass().getName() + ".ChangelistBuilder");
	@NotNull
	private final MksVcs mksvcs;

	public MKSChangeProvider(@NotNull MksVcs mksvcs) {
		super(mksvcs.getProject());
		this.mksvcs = mksvcs;
	}


	public void getChanges(final VcsDirtyScope dirtyScope, ChangelistBuilder builder, final ProgressIndicator progress) throws VcsException {
		ArrayList<VcsException> errors = new ArrayList<VcsException>();
		logger.debug("start getChanges");
		try {
			ArrayList<MksServerInfo> servers = getMksServers(progress, errors);
			final Map<MksServerInfo, Map<String, MksChangePackage>> changePackagesPerServer = getChangePackages(progress, errors, servers);
			// collect affected sandboxes
			final ChangelistBuilder myBuilder = builder;
			if (MksVcs.DEBUG) {
				builder = createBuilderLoggingProxy(myBuilder);
			}
			final Map<String, MksServerInfo> serversByHostAndPort = distributeServersByHostAndPort(servers);
			final Map<MksServerInfo, Map<String, MksMemberState>> states = new HashMap<MksServerInfo, Map<String, MksMemberState>>();
			final SandboxCache sandboxCache = mksvcs.getSandboxCache();
			for (VirtualFile dir : dirtyScope.getAffectedContentRoots()) {
				Set<MksSandboxInfo> sandboxes = sandboxCache.getSandboxesIntersecting(dir);
				for (MksSandboxInfo sandbox : sandboxes) {
					MksServerInfo sandboxServer = serversByHostAndPort.get(sandbox.hostAndPort);
					if (states.get(sandboxServer) == null) {
						states.put(sandboxServer, new HashMap<String, MksMemberState>());
					}
					states.get(sandboxServer).putAll(getSandboxState(sandbox, errors, sandboxServer));
				}
			}
			final ChangelistBuilder finalBuilder = builder;
			// iterate over the local dirty scope to flag unversioned files
			dirtyScope.iterate(new Processor<FilePath>() {
				public boolean process(FilePath filePath) {
					if (filePath.isDirectory()) {
//                        System.err.println("skipping directory");
						return true;
					} else if (filePath.getVirtualFile() == null) {
						logger.warn("no VirtualFile for " + filePath.getPath() + ", ignoring");
//                        finalBuilder.processIgnoredFile(filePath.getVirtualFile());
						return true;
					} else if (sandboxCache.isSandboxProject(filePath.getVirtualFile())) {
						finalBuilder.processIgnoredFile(filePath.getVirtualFile());
//                        System.err.println("ignoring project.pj file");
						return true;
					} else {
						MksSandboxInfo sandbox = sandboxCache.getSandboxInfo(filePath.getVirtualFile());
						if (sandbox != null) {
							MksServerInfo sandboxServer = serversByHostAndPort.get(sandbox.hostAndPort);
							if (sandboxServer != null) {
								Map<String, MksMemberState> serverStates = states.get(sandboxServer);
								if (serverStates == null) {
									logger.info("no sandbox states for server " + sandboxServer + " (used by sandbox " + sandbox + ")");
									finalBuilder.processUnversionedFile(filePath.getVirtualFile());
									return true;
								}
								MksMemberState state = serverStates.get(filePath.getPath());
								if (state != null) {
									return true;
								}
								final String absolutePath = filePath.getPath().toUpperCase();
								// there is possibly a file with a different case ...
								for (String filename : serverStates.keySet()) {
									if (filename.toUpperCase().equals(absolutePath) && new File(filename).equals(filePath.getIOFile())) {
										logger.warn("found a file with different case for " + filename);
										return true;
									}
								}
							} else {
								// should not happen, but who knows !
								logger.warn("no known server for sandbox [" + sandbox.sandboxPath + "]");
							}
						}
					}
					// either
					// no state for a sandbox controlled file
					// or file not sandbox
					// or no known server for a reported sandbox
					finalBuilder.processUnversionedFile(filePath.getVirtualFile());
					return true;
				}
			});
			for (Map.Entry<MksServerInfo, Map<String, MksMemberState>> entry : states.entrySet()) {
				MksServerInfo sandboxServer = entry.getKey();
				processDirtySandbox(builder, changePackagesPerServer.get(sandboxServer), entry.getValue());
			}
		} catch (RuntimeException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			throw e;
		} finally {
			logger.debug("end getChanges");
		}
		if (!errors.isEmpty()) {
			mksvcs.showErrors(errors, "ChangeProvider");
		}


	}

	private ChangelistBuilder createBuilderLoggingProxy(final ChangelistBuilder myBuilder) {
		return (ChangelistBuilder) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ChangelistBuilder.class}, new InvocationHandler() {
			public Object invoke(final Object o, final Method method, final Object[] args) throws Throwable {
				StringBuffer buf = new StringBuffer("(");
				for (Object arg : args) {
					buf.append(arg).append(",");
				}
				buf.setLength(buf.length() - 1);
				buf.append(")");
				BUILDER_PROXY_LOGGER.debug(method.getName() + buf);
				return method.invoke(myBuilder, args);
			}
		});
	}

	private Map<String, MksServerInfo> distributeServersByHostAndPort(final ArrayList<MksServerInfo> servers) {
		Map<String, MksServerInfo> result = new HashMap<String, MksServerInfo>();
		for (MksServerInfo server : servers) {
			result.put(server.host + ":" + server.port, server);
		}
		return result;
	}

	private void processDirtySandbox(final ChangelistBuilder builder, final Map<String, MksChangePackage> changePackages,
									 final Map<String, MksMemberState> states) {
		for (Map.Entry<String, MksMemberState> entry : states.entrySet()) {
			MksMemberState state = entry.getValue();
			FilePath filePath = VcsUtil.getFilePath(entry.getKey());
			VirtualFile virtualFile = VcsUtil.getVirtualFile(entry.getKey());
			switch (state.status) {
				case ADDED: {
					MksChangePackage changePackage = getChangePackage(changePackages, state);
					Change change = new Change(
							new MksContentRevision(mksvcs, filePath, state.workingRevision),
							CurrentContentRevision.create(filePath),
							FileStatus.ADDED);
					if (changePackage == null) {
						builder.processChange(change);
					} else {
						ChangeList changeList = mksvcs.getChangeListAdapter().trackMksChangePackage(changePackage);
						builder.processChangeInList(change, changeList);
					}
					break;
				}
				case CHECKED_OUT: {
					MksChangePackage changePackage = getChangePackage(changePackages, state);
					Change change = new Change(
							new MksContentRevision(mksvcs, filePath, state.workingRevision),
							CurrentContentRevision.create(filePath),
							FileStatus.MODIFIED);
					if (changePackage == null) {
						builder.processChange(change);
					} else {
						ChangeList changeList = mksvcs.getChangeListAdapter().trackMksChangePackage(changePackage);
						builder.processChangeInList(change, changeList);
					}
					break;
				}
				case MODIFIED_WITHOUT_CHECKOUT: {
					builder.processModifiedWithoutCheckout(virtualFile);
					break;
				}
				case MISSISNG: {
					builder.processLocallyDeletedFile(filePath);
					break;
				}
				case SYNC:
					builder.processChange(new Change(
							new MksContentRevision(mksvcs, filePath, state.workingRevision),
							new MksContentRevision(mksvcs, filePath, state.memberRevision),
							FileStatus.OBSOLETE));
					break;
				case DROPPED: {
					MksChangePackage changePackage = getChangePackage(changePackages, state);
					Change change = new Change(
							new MksContentRevision(mksvcs, filePath, state.workingRevision),
							CurrentContentRevision.create(filePath),
							FileStatus.DELETED);
					if (changePackage == null) {
						builder.processChange(change);
					} else {
						ChangeList changeList = mksvcs.getChangeListAdapter().trackMksChangePackage(changePackage);
						builder.processChangeInList(change, changeList);
					}
					break;
				}
				case NOT_CHANGED:
					break;
				case UNKNOWN: {
					builder.processChange(new Change(
							new MksContentRevision(mksvcs, filePath, state.workingRevision),
							new MksContentRevision(mksvcs, filePath, state.memberRevision),
							FileStatus.UNKNOWN));
				}
				default: {
					logger.info("unhandled MKS status " + state.status);
				}
			}
		}
	}

	private MksChangePackage getChangePackage(final Map<String, MksChangePackage> changePackages, final MksMemberState state) {
		return state.workingChangePackageId == null ? null : changePackages.get(state.workingChangePackageId);
	}

	private Map<String, MksMemberState> getSandboxState(@NotNull final MksSandboxInfo sandbox, final ArrayList<VcsException> errors, final MksServerInfo server) {
		Map<String, MksMemberState> states = new HashMap<String, MksMemberState>();

		ViewSandboxWithoutChangesCommand fullSandboxCommand = new ViewSandboxWithoutChangesCommand(errors, mksvcs, server.user, sandbox.sandboxPath);
		fullSandboxCommand.execute();
		states.putAll(fullSandboxCommand.getMemberStates());

		ViewSandboxOutOfSyncCommand outOfSyncCommand = new ViewSandboxOutOfSyncCommand(errors, mksvcs, server.user, sandbox.sandboxPath);
		outOfSyncCommand.execute();
		states.putAll(outOfSyncCommand.getMemberStates());

		ViewSandboxLocalChangesCommand localChangesCommand = new ViewSandboxLocalChangesCommand(errors, mksvcs, server.user, sandbox.sandboxPath);
		localChangesCommand.execute();
		states.putAll(localChangesCommand.getMemberStates());

		ViewSandboxMissingCommand missingCommand = new ViewSandboxMissingCommand(errors, mksvcs, server.user, sandbox.sandboxPath);
		missingCommand.execute();
		states.putAll(missingCommand.getMemberStates());

		return states;
	}

	/**
	 * @param progress
	 * @param errors
	 * @param servers
	 * @return Map <MksServerInfo, Map<MksChangePackage.id,MksChangePackage>>
	 */
	private Map<MksServerInfo, Map<String, MksChangePackage>> getChangePackages(final ProgressIndicator progress, final ArrayList<VcsException> errors, final ArrayList<MksServerInfo> servers) {
		final Map<MksServerInfo, Map<String, MksChangePackage>> changePackages = new HashMap<MksServerInfo, Map<String, MksChangePackage>>();
		for (MksServerInfo server : servers) {
			final ListChangePackages listCpsAction = new ListChangePackages(errors, mksvcs, server);
			if (progress != null) {
				progress.setIndeterminate(true);
				progress.setText("Querying change packages for " + server + "...");
			}
			listCpsAction.execute();
			if (listCpsAction.foundError()) {
				logger.warn("error querying mks cps");
			}
			Map<String, MksChangePackage> serverChangePackages = new HashMap<String, MksChangePackage>();
			for (MksChangePackage changePackage : listCpsAction.changePackages) {
				serverChangePackages.put(changePackage.getId(), changePackage);
			}
			changePackages.put(server, serverChangePackages);
		}
		return changePackages;
	}

	private ArrayList<MksServerInfo> getMksServers(final ProgressIndicator progress, final ArrayList<VcsException> errors) {
		final ListServers listServersAction = new ListServers(errors, mksvcs);
		if (progress != null) {
			progress.setIndeterminate(true);
			progress.setText("Querying mks servers ...");
		}
		listServersAction.execute();
		if (listServersAction.foundError()) {
			logger.warn("encountered errors querying servers");
		}
		return listServersAction.servers;
	}

	public boolean isModifiedDocumentTrackingRequired() {
		return false;
	}

	@NotNull
	public MksChangeListAdapter getChangeListAdapter() {
		return mksvcs.getChangeListAdapter();
	}


	public void decorateChangeList(LocalChangeList changeList, ColoredTreeCellRenderer cellRenderer, boolean selected, boolean expanded, boolean hasFocus) {
		MksChangeListAdapter changeListAdapter = getChangeListAdapter();
		if (!changeListAdapter.isChangeListMksControlled(changeList.getName())) {
			return;
		}
		MksChangePackage aPackage = changeListAdapter.getMksChangePackage(changeList.getName());
		if (aPackage != null) {

			cellRenderer.append(" - MKS #" + aPackage.getId(), SimpleTextAttributes.GRAY_ATTRIBUTES);
		}
	}
}

