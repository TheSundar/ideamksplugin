package org.intellij.vcs.mks.sicommands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.MksBundle;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksConfiguration;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.jetbrains.annotations.NonNls;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This command uses tabs to separate fields ...
 */
public class ViewMemberHistoryCommand extends SiCLICommand {
	@NonNls
	public static final String COMMAND = "viewhistory";
	private static final String descriptionPattern = "(.*)";
	private static final int REVISION_IDX = 1;
	private static final int DATE_IDX = 2;
	private static final int AUTHOR_IDX = 3;
	private static final int CPID_IDX = 4;
	private static final int DESCRIPTION_IDX = 5;

	@NonNls
	private static final String datePattern = "([^\\t+]*)";
	@NonNls
	public static final String wholeLinePatternString =
			revisionPattern + "\\t"
					+ datePattern + "\\t"
					+ userPattern + "\\t"
					+ changePackageIdPattern + "\\t"
					+ descriptionPattern;
	private List<MksMemberRevisionInfo> revisionsInfo = new ArrayList<MksMemberRevisionInfo>();
	private DateFormat format;
	private final String member;

	public ViewMemberHistoryCommand(List<VcsException> errors, MksCLIConfiguration mksCLIConfiguration, String member) {
		super(errors, mksCLIConfiguration, COMMAND,
				"--fields=revision,date,author,cpid,description",
				member);
		setWorkingDir(new File(member).getParentFile());
		this.member = member;
		format = new SimpleDateFormat(mksCLIConfiguration.getDatePattern());
	}

	@Override
	public void execute() {
		try {
			executeCommand();
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
			return;
		}
		Pattern wholeLinePattern = Pattern.compile(wholeLinePatternString);
		BufferedReader reader = new BufferedReader(new StringReader(commandOutput));
		try {
			String firstLine = reader.readLine();
			if (firstLine == null || !VcsUtil.getFilePath(member).equals(VcsUtil.getFilePath(firstLine))) {
				LOGGER.warn("unexpected command output " + commandOutput + ", first line is expected to be " + member);
			} else {
				String line;
				while ((line = reader.readLine()) != null) {
					Matcher matcher = wholeLinePattern.matcher(line);
					try {
						if (matcher.matches()) {
							String rev = matcher.group(REVISION_IDX);
							String date = matcher.group(DATE_IDX);
							String cpid = matcher.group(CPID_IDX);
							String author = matcher.group(AUTHOR_IDX);
							String description = matcher.group(DESCRIPTION_IDX);
							MksMemberRevisionInfo info = new MksMemberRevisionInfo();
							info.setRevision(createRevision(rev));
							info.setDate(parseDate(date, true));
							info.setCPID(cpid);
							info.setAuthor(author);
							info.setDescription(description);
							revisionsInfo.add(info);
						} else if (line.startsWith("Member added")) {
							// ignore this line and any following
							//noinspection StatementWithEmptyBody
							while (reader.readLine() != null) {
							}
						} else if (revisionsInfo.size() >= 1) {
							LOGGER.debug("assuming multiline comment :" + line);
							final MksMemberRevisionInfo info = revisionsInfo.get(revisionsInfo.size() - 1);
							info.setDescription(info.getDescription() + "\n" + line);
						} else {
							//noinspection ThrowableInstanceNeverThrown
							errors.add(new VcsException("ViewMemberHistory: unexpected line [" + line + "]"));
						}
					} catch (VcsException e) {
						errors.add(e);
					}
				}
			}
		} catch (IOException e) {
			//noinspection ThrowableInstanceNeverThrown
			errors.add(new VcsException(e));
		}
	}

	protected void handleErrorOutput(String errorOutput) {
		if (exitValue == 128 && errorOutput.contains("is not a current or destined or pending member")) {
			errors.add(new VcsException(GetRevisionInfo.NOT_A_MEMBER));
			return;
		} else {
			super.handleErrorOutput(
					errorOutput);	//To change body of overridden methods use File | Settings | File Templates.
		}
	}

	private Date parseDate(String date, boolean updatePatternIfNeeded) throws VcsException {
		try {
			return format.parse(date);
		} catch (ParseException e) {

			if (updatePatternIfNeeded) {
				final String pattern = Messages.showInputDialog(
						MksBundle.message("configuration.datepattern.incorrect.message", date,
								mksCLIConfiguration.getDatePattern()),
						MksBundle.message("configuration.datepattern.incorrect.title"), Messages.getErrorIcon(),
						mksCLIConfiguration.getDatePattern(),
						new MksConfiguration.DatePatternValidator());
				if (pattern != null) {
					ApplicationManager.getApplication().getComponent(MksConfiguration.class).setDatePattern(pattern);
					format = new SimpleDateFormat(mksCLIConfiguration.getDatePattern());
				}
				return parseDate(date, false);

			} else {
				throw new VcsException(
						"unknown date format for " + date + " (expected [" + mksCLIConfiguration.getDatePattern() +
								"]). " +
								"This may be an encoding issue, encoding used was " +
								mksCLIConfiguration.getMksSiEncoding(COMMAND));

			}
		}
	}

	public List<MksMemberRevisionInfo> getRevisionsInfo() {
		return revisionsInfo;
	}
}
