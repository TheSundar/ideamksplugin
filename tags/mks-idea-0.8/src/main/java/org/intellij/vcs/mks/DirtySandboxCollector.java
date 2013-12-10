/*
 * COPYRIGHT. HSBC HOLDINGS PLC 2008. ALL RIGHTS RESERVED.
 *
 * This software is only to be used for the purpose for which it has been
 * provided. No part of it is to be reproduced, disassembled, transmitted,
 * stored in a retrieval system nor translated in any human or computer
 * language in any way or for any other purposes whatsoever without the
 * prior written consent of HSBC Holdings plc.
 */
package org.intellij.vcs.mks;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.VcsDirtyScope;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.realtime.MksSandboxInfo;
import org.intellij.vcs.mks.realtime.SandboxCache;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class DirtySandboxCollector {
	private final Project myProject;

	public DirtySandboxCollector(Project myProject) {
		this.myProject = myProject;
	}

	@NotNull
	public SandboxesToRefresh collectAffectedSandboxes(@NotNull VcsDirtyScope dirtyScope) {
		final Set<FilePath> recursivelyDirtyDirectories = dirtyScope.getRecursivelyDirtyDirectories();
		final SandboxesToRefresh sandboxesToRefresh = new SandboxesToRefresh();
		final SandboxCache cache = MksVcs.getInstance(myProject).getSandboxCache();
		for (FilePath directory : recursivelyDirtyDirectories) {

			final VirtualFile dirVFile = directory.getVirtualFile();
			if (dirVFile == null) {
//				System.err.println("no vfile for " + directory);
			} else {
				processRecursively(dirVFile, cache, sandboxesToRefresh);
			}
		}
		for (FilePath path : dirtyScope.getDirtyFiles()) {
			final VirtualFile file = path.getVirtualFile();
			if (null == file) {
//				System.err.println("no vfile for " + file);
			} else {
				processRecursively(file, cache, sandboxesToRefresh);
			}
		}

//		System.out.println("refreshSpec " + sandboxesToRefresh.toString());
		return sandboxesToRefresh;
	}

	private void processRecursively(@NotNull final VirtualFile vFile,
									@NotNull final SandboxCache cache,
									@NotNull final SandboxesToRefresh refresh) {
		final MksSandboxInfo owningSandbox = cache.getSandboxInfo(vFile);
		if (owningSandbox != null) {
			final Module module = ModuleUtil.findModuleForFile(vFile, myProject);
			if (module == null) {
				refresh.addNonRecursive(owningSandbox, vFile);
			} else {
				refresh.refresh(owningSandbox, ModuleRootManager.getInstance(module).getExcludeRoots());
			}
		} else if (vFile.isDirectory()) {
			for (VirtualFile child : vFile.getChildren()) {
				if (child.isDirectory()) {
					processRecursively(child, cache, refresh);
				}
			}
		}

	}

	static final class SandboxesToRefresh {
		final Map<MksSandboxInfo, ArrayList<VirtualFile>> excludedRootsPerSandbox =
				new HashMap<MksSandboxInfo, ArrayList<VirtualFile>>();
		private Map<MksSandboxInfo, HashSet<VirtualFile>> recursivelyIncludedRootsPerSandbox =
				new HashMap<MksSandboxInfo, HashSet<VirtualFile>>();
		private Map<MksSandboxInfo, HashSet<VirtualFile>> nonRecursivelyIncludedRootsPerSandbox =
				new HashMap<MksSandboxInfo, HashSet<VirtualFile>>();
		private boolean computed = false;

		public void refresh(MksSandboxInfo sandbox, VirtualFile[] excludedRoots) {
			assert !computed : "too late !";
			ArrayList<VirtualFile> excluded = excludedRootsPerSandbox.get(sandbox);
			if (excluded == null) {
				excluded = new ArrayList<VirtualFile>();
				excludedRootsPerSandbox.put(sandbox, excluded);
			}
			for (VirtualFile excludedRoot : excludedRoots) {
				if (excludedRoot.isDirectory() && sandbox.contains(excludedRoot)) {
					excluded.add(excludedRoot);
				}
			}
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("SandboxesToRefresh[");
			for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : excludedRootsPerSandbox
					.entrySet()) {
				buf.append(entry.getKey());
				final ArrayList<VirtualFile> excludedRoots = entry.getValue();
				if (!excludedRoots.isEmpty()) {
					buf.append("{");
					for (VirtualFile root : excludedRoots) {
						buf.append(root).append(",");
					}
					buf.append("}");
				}
				buf.append("\n");
			}
			buf.append("]");
			return buf.toString();
		}

		public Set<MksSandboxInfo> getSandboxes() {
			return excludedRootsPerSandbox.keySet();
		}

		public ArrayList<VirtualFile> getExcludedRoots(MksSandboxInfo sandbox) {
			return excludedRootsPerSandbox.get(sandbox);
		}

		/**
		 * returns a list a directories where each one needs to be refreshed inclusively
		 *
		 * @param sandbox
		 * @return
		 */
		public HashSet<VirtualFile> getRecursivelyIncludedRoots(MksSandboxInfo sandbox) {
			computeInclusions();
			final HashSet<VirtualFile> files = recursivelyIncludedRootsPerSandbox.get(sandbox);
			return files == null ? new HashSet<VirtualFile>() : files;
		}

		public void addNonRecursive(MksSandboxInfo owningSandbox, VirtualFile vFile) {
			assert !computed : "too late !";
			if (vFile.isDirectory()) {
				addAndCreateSetIfAbsent(nonRecursivelyIncludedRootsPerSandbox, owningSandbox, vFile);
			}
		}

		private static void addAndCreateSetIfAbsent(
				Map<MksSandboxInfo, HashSet<VirtualFile>> map,
				MksSandboxInfo sandboxInfo, VirtualFile root) {
			HashSet<VirtualFile> files = map.get(sandboxInfo);
			if (files == null) {
				files = new HashSet<VirtualFile>();
				map.put(sandboxInfo, files);
			}
			files.add(root);
		}

		private final static class DirectoryWalker {
			public void process(SandboxesToRefresh refreshSpec) {
				for (Map.Entry<MksSandboxInfo, ArrayList<VirtualFile>> entry : refreshSpec
						.excludedRootsPerSandbox.entrySet()) {
					final VirtualFile root = entry.getKey().getSandboxDir();
					processRecursivelyExcluding(entry.getKey(), root, entry.getValue(), refreshSpec);
				}
			}

			private void processRecursivelyExcluding(MksSandboxInfo sandboxInfo, VirtualFile root,
													 ArrayList<VirtualFile> excluded,
													 SandboxesToRefresh spec) {
				boolean containsExcluded = false;
				if (root.isDirectory()) {
					if (excluded.contains(root)) {
						return;
					}
					for (VirtualFile file : excluded) {
						if (file.isDirectory() && VfsUtil.isAncestor(root, file, false)) {
							containsExcluded = true;
							break;
						}
					}
					if (containsExcluded) {
//						System.out.println("adding " + root + " to nonRecursivelyIncludedRootsPerSandbox for " +
//								sandboxInfo.getSandboxDir());
						addAndCreateSetIfAbsent(spec.nonRecursivelyIncludedRootsPerSandbox, sandboxInfo, root);
						for (VirtualFile child : root.getChildren()) {
							if (child.isDirectory()) {
								processRecursivelyExcluding(sandboxInfo, child, excluded, spec);
							}
						}
					} else {
//						System.out.println("adding " + root + " to recursivelyIncludedRootsPerSandbox for " +
//								sandboxInfo.getSandboxDir());
						addAndCreateSetIfAbsent(spec.recursivelyIncludedRootsPerSandbox, sandboxInfo, root);
					}
				}
			}

		}

		private void computeInclusions() {
			if (computed) {
				return;
			}
			final DirectoryWalker walker = new DirectoryWalker();
			walker.process(this);
			computed = true;

		}

		/**
		 * returns a list a directories where each one should be refreshed NOT inclusively
		 *
		 * @param sandbox
		 * @return
		 */
		public HashSet<VirtualFile> getNonRecursivelyIncludedRoots(MksSandboxInfo sandbox) {
			computeInclusions();
			final HashSet<VirtualFile> files = nonRecursivelyIncludedRootsPerSandbox.get(sandbox);
			return files == null ? new HashSet<VirtualFile>() : files;
		}
	}
}
