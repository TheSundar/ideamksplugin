package org.intellij.vcs.mks.update;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.*;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksMemberState;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.sicommands.cli.AbstractViewSandboxCommand;
import org.intellij.vcs.mks.sicommands.cli.ViewSandboxRemoteChangesCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MksUpdateEnvironment implements UpdateEnvironment {
	private final MksVcs mksVcs;
	private final Logger LOGGER = Logger.getInstance(MksUpdateEnvironment.class.getName());

	public MksUpdateEnvironment(final MksVcs mksVcs) {
		this.mksVcs = mksVcs;
	}

	@Nullable
	public Configurable createConfigurable(final Collection<FilePath> filePaths) {
//		System.out.println("MksUpdateEnvironment.createConfigurable " + System.currentTimeMillis());
		return null;
	}

	public void fillGroups(final UpdatedFiles updatedFiles) {
//		System.out.println("MksUpdateEnvironment.fillGroups " + System.currentTimeMillis());
//		System.out.println("updatedFiles = " + updatedFiles);
//		new Exception().printStackTrace();
	}

	/**
	 * si viewsandbox --filter=changed:newmem : files remotely added
	 * si viewsandbox --filter=changed:sync files with a new revision on server
	 * si viewsandbox --filter=changed:missing : files locally deleted
	 * si viewsandbox --filter=changed:working files locally modified (with or without lock)
	 * si viewsandbox --filter=changed:newer  ????
	 * <p/>
	 * Changes happening from server shoudl be --filter=changed:newmem,changed:sync
	 * Changes from local should be --filter=deferred,changed:missing,changed:working
	 * <p/>
	 * do not forget deferred filter when requesting local changes
	 *
	 * @param filePaths
	 * @param updatedFiles
	 * @param progressIndicator
	 * @return
	 * @throws ProcessCanceledException
	 */
	@NotNull
	public UpdateSession updateDirectories(@NotNull final FilePath[] filePaths, final UpdatedFiles updatedFiles,
										   final ProgressIndicator progressIndicator,
										   @NotNull final Ref<SequentialUpdatesContext> sequentialUpdatesContextRef) throws ProcessCanceledException {
//		System.out.println("MksUpdateEnvironment.updateDirectories " + System.currentTimeMillis());
		final Set<MksSandboxInfo> sandboxes = new HashSet<MksSandboxInfo>();
		for (final FilePath filePath : filePaths) {
			if (filePath.isDirectory()) {
				sandboxes.addAll(mksVcs.getSandboxCache().getSandboxesIntersecting(filePath.getVirtualFile()));
			} else {
				sandboxes.addAll(mksVcs.getSandboxCache().getSandboxesIntersecting(filePath.getVirtualFileParent()));
			}
		}

		final ArrayList<VcsException> exceptions = new ArrayList<VcsException>();
		for (final MksSandboxInfo sandbox : sandboxes) {
			final AbstractViewSandboxCommand command =
					new ViewSandboxRemoteChangesCommand(exceptions, mksVcs, sandbox.sandboxPath);
			command.execute();
			for (final Map.Entry<String, MksMemberState> entry : command.getMemberStates().entrySet()) {
				final MksMemberState state = entry.getValue();
				switch (state.status) {
					case REMOTELY_ADDED:
						updatedFiles.getGroupById(FileGroup.CREATED_ID)
								.add(entry.getKey(), MksVcs.OUR_KEY, state.memberRevision);
						break;
					case REMOTELY_DROPPED:
						updatedFiles.getGroupById(FileGroup.REMOVED_FROM_REPOSITORY_ID)
								.add(entry.getKey(), MksVcs.OUR_KEY, state.memberRevision);
						break;
					case DROPPED:
						updatedFiles.getGroupById(FileGroup.LOCALLY_REMOVED_ID).add(entry.getKey(), MksVcs.OUR_KEY, null);
						break;
					case SYNC:
						updatedFiles.getGroupById(FileGroup.UPDATED_ID)
								.add(entry.getKey(), MksVcs.OUR_KEY, state.memberRevision);
						break;
					default:
						LOGGER.warn("unexpected status " + state.status + " for " + entry.getKey());
						updatedFiles.getGroupById(FileGroup.UNKNOWN_ID).add(entry.getKey(), MksVcs.OUR_KEY, null);
				}
			}
		}

/*
		for (MksNativeSandboxInfo sandbox : sandboxes) {
			AbstractViewSandboxCommand command =
					new ViewSandboxLocalChangesCommand(exceptions, mksVcs, sandbox.sandboxPath);
			command.execute();
			for (Map.Entry<String, MksMemberState> entry : command.getMemberStates().entrySet()) {
				final MksMemberState state = entry.getValue();
				switch (state.status) {
					case UNVERSIONED:
						updatedFiles.getGroupById(FileGroup.UNKNOWN_ID).add(entry.getKey(), mksVcs, state.memberRevision);
						break;
					case MODIFIED_WITHOUT_CHECKOUT:
						updatedFiles.getGroupById(FileGroup.LOCALLY_REMOVED_ID).add(entry.getKey());
						break;
					case SYNC:
						updatedFiles.getGroupById(FileGroup.UPDATED_ID).add(entry.getKey(), mksVcs, state.memberRevision);
						break;
					default:
						LOGGER.warn("unexpected status " + state.status + " for " + entry.getKey());
						updatedFiles.getGroupById(FileGroup.UNKNOWN_ID).add(entry.getKey());
				}
			}
		}
*/

/*
		for (Map.Entry<String, MksMemberState> entry : remoteStates.entrySet()) {
			final MksMemberState state = entry.getValue();
		}
*/

//		for (FileGroup fileGroup : updatedFiles.getTopLevelGroups()) {
//			System.out.println(fileGroup);
//		}
//		System.out.println(updatedFiles.getGroupById(FileGroup.REMOVED_FROM_REPOSITORY_ID));
//		System.out.println(updatedFiles.getGroupById(FileGroup.CREATED_ID));
		return new UpdateSession() {
			@NotNull
			public List<VcsException> getExceptions() {
//				System.out.println("MksUpdateEnvironment.getExceptions " + System.currentTimeMillis());
				return exceptions;
			}

			public void onRefreshFilesCompleted() {
//				System.out.println("MksUpdateEnvironment.onRefreshFilesCompleted  " + System.currentTimeMillis());
			}

			public boolean isCanceled() {
//				System.out.println("MksUpdateEnvironment.isCanceled  " + System.currentTimeMillis());
				return false;
			}
		};
	}

	/**
	 * todo
	 *
	 * @param filePaths
	 * @return
	 */
	public boolean validateOptions(final Collection<FilePath> filePaths) {
		return true;
	}
}
