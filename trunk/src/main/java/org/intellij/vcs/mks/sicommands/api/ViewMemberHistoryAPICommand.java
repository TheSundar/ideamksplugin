/*
 * COPYRIGHT. HSBC HOLDINGS PLC 2008. ALL RIGHTS RESERVED.
 *
 * This software is only to be used for the purpose for which it has been
 * provided. No part of it is to be reproduced, disassembled, transmitted,
 * stored in a retrieval system nor translated in any human or computer
 * language in any way or for any other purposes whatsoever without the
 * prior written consent of HSBC Holdings plc.
 */
package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.*;
import com.mks.api.response.*;
import org.intellij.vcs.mks.AbstractMKSCommand;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.MksVcsException;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ViewMemberHistoryAPICommand extends SiAPICommand {

	private final String member;
	private List<MksMemberRevisionInfo> revisionsInfo;
	private static final String COMMAND = "viewhistory";
	@NonNls
	private static final String REVISION = "revision";
	@NonNls
	private static final String DATE = "date";
	@NonNls
	private static final String AUTHOR = "author";
	@NonNls
	private static final String CPID = "cpid";
	@NonNls
	private static final String DESCRIPTION = "description";
	@NonNls
	private static final String FIELDS = REVISION + "," + DATE + "," + AUTHOR + "," + CPID + "," + DESCRIPTION;

	public ViewMemberHistoryAPICommand(List<VcsException> errors, MksCLIConfiguration mksCLIConfiguration,
									   String member) {
		super(errors, COMMAND, mksCLIConfiguration);
		this.member = member;
	}

	public void execute() {
		try {
			Command command = new Command(Command.SI);
			command.setCommandName(COMMAND);
			command.addOption(new Option("fields", FIELDS));
			command.addSelection(member);
			final Response response;
			/**
			 * response ::= WorkItem[id = membername]{revision*}
			 * revision ::= Field
			 */
			response = executeCommand(command);
			final WorkItemIterator workItems = response.getWorkItems();
			final List memberRevisions = (List) ((WorkItem) workItems.next()).getField("revisions").getValue();
			revisionsInfo = new ArrayList<MksMemberRevisionInfo>(memberRevisions.size());
			for (Iterator itRevisions = memberRevisions.iterator(); itRevisions.hasNext();) {
				Item revisionItem = (Item) itRevisions.next();
				String revision = revisionItem.getField(REVISION).getValueAsString();
				final Date date = revisionItem.getField(DATE).getDateTime();
				final String author = revisionItem.getField(AUTHOR).getValueAsString();
				final String cpid = revisionItem.getField(CPID).getValueAsString();
				final String description = revisionItem.getField(DESCRIPTION).getValueAsString();
				MksMemberRevisionInfo info = new MksMemberRevisionInfo();
				info.setRevision(MksRevisionNumber.createRevision(revision));
				info.setDate(date);
				info.setCPID(cpid);
				info.setAuthor(author);
				info.setDescription(description);
				revisionsInfo.add(info);
			}
		} catch (APIException e) {

			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			errors.add(new MksVcsException("apiException", e));
		} catch (VcsException e) {
			errors.add(e);
		}
	}

	public List<MksMemberRevisionInfo> getRevisionsInfo() {
		return revisionsInfo;
	}
}
