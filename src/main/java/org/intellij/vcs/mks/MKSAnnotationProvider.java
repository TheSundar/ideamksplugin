/*
 * COPYRIGHT. HSBC HOLDINGS PLC 0000. ALL RIGHTS RESERVED.
 *
 * This software is only to be used for the purpose for which it has been
 * provided. No part of it is to be reproduced, disassembled, transmitted,
 * stored in a retrieval system nor translated in any human or computer
 * language in any way or for any other purposes whatsoever without the( prior)?
 * (prior )?written consent of HSBC Holdings plc.
 */
package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.vcs.mks.history.MksFileAnnotation;
import org.intellij.vcs.mks.history.MksVcsFileRevision;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.intellij.vcs.mks.sicommands.api.ViewMemberHistoryAPICommand;
import org.intellij.vcs.mks.sicommands.cli.AnnotateFileCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MKSAnnotationProvider implements AnnotationProvider {
	private MksVcs mksVcs;

	public MKSAnnotationProvider(MksVcs mksVcs) {
		this.mksVcs = mksVcs;
	}

	@Override
	public FileAnnotation annotate(VirtualFile file) throws VcsException {
		return annotate(file, null);
	}

	private AnnotateFileCommand createAnnotateCommand(VirtualFile file, ArrayList<VcsException> errors, VcsFileRevision revision) {
		return (revision == null)
				? new AnnotateFileCommand(errors, mksVcs, file.getCanonicalPath())
				: new AnnotateFileCommand(errors, mksVcs, file.getCanonicalPath(), (MksRevisionNumber) revision.getRevisionNumber());
	}

	@Override
	public FileAnnotation annotate(VirtualFile file, VcsFileRevision revision) throws VcsException {
		ArrayList<VcsException> errors = new ArrayList<VcsException>();
		AnnotateFileCommand command = createAnnotateCommand(file, errors, revision);
		command.execute();

		ArrayList<VcsFileRevision> fileRevisions = new ArrayList<VcsFileRevision>();
		HashSet<VcsRevisionNumber> revisionSet = new HashSet<VcsRevisionNumber>();
		revisionSet.addAll(command.getRevisions());

		// collect commit info for the revisions involved
		ViewMemberHistoryAPICommand memberHistoryCommand = new ViewMemberHistoryAPICommand(errors, mksVcs, file.getCanonicalPath());
		memberHistoryCommand.execute();
		List<MksMemberRevisionInfo> revisionsInfo = memberHistoryCommand.getRevisionsInfo();
		for (MksMemberRevisionInfo revisionInfo : revisionsInfo) {
			if (revisionSet.contains(revisionInfo.getRevision())) {
				fileRevisions.add(new MksVcsFileRevision(MksVcs.getInstance(mksVcs.getProject()), VcsContextFactory.SERVICE.getInstance().createFilePathOn(file), revisionInfo));
			}
		}

		return new MksFileAnnotation(mksVcs.getProject(), file, command.getLineInfos(), command.getLines(), command.getRevisions(), fileRevisions);
	}

	@Override
	public boolean isAnnotationValid(VcsFileRevision rev) {
		return true;
	}
}
