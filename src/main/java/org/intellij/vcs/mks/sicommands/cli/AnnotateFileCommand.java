package org.intellij.vcs.mks.sicommands.cli;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.history.MksFileAnnotation;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotateFileCommand extends SiCLICommand {
    public static final String pattern = "^([^\\t]*)\\t([^\\t]*)\\t([^\\t]*)\\t([^\\t]*)\\t([^\\t]*)\\t(.*)$";
    public static final int AUTHOR_GROUP = 1;
    public static final int CPID_GROUP = AUTHOR_GROUP+1;
    public static final int DATE_GROUP = CPID_GROUP+1;
    public static final int LINENUM_GROUP =DATE_GROUP+1;
    public static final int REVISION_GROUP = LINENUM_GROUP+1;
    public static final int TEXT_GROUP = REVISION_GROUP+1;
    private List<MksFileAnnotation.LineInfo> lineInfos = new ArrayList<MksFileAnnotation.LineInfo>();
    private List<String> lines  = new ArrayList<String>();
    private Map<String, VcsRevisionNumber> revisions = new HashMap<String, VcsRevisionNumber>();

    public AnnotateFileCommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration, @NonNls String member) {
        super(errors, mksCLIConfiguration, "annotate", "--fields=author,cpid,date,linenum,revision,text", member);
    }
    public AnnotateFileCommand(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration, @NonNls String member, MksRevisionNumber revision) {
        super(errors, mksCLIConfiguration, "annotate", "--fields=author,cpid,date,linenum,revision,text", "-r", revision.asString(), member);
    }

    @Override
    public void execute() {
        Pattern wholeLinePattern = Pattern.compile(pattern);
        try {
            executeCommand();
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
        BufferedReader reader = new BufferedReader(new StringReader(commandOutput));
        String line ;
        try {

            DateFormat df = new SimpleDateFormat("MMM dd, yyyy Z", this.mksCLIConfiguration.getDateLocale());
            while (null != (line = reader.readLine())) {
                Matcher matcher = wholeLinePattern.matcher(line);
                if (matcher.matches()) {
                    String author = matcher.group(AUTHOR_GROUP);
                    String cpid = matcher.group(CPID_GROUP);
                    String date = matcher.group(DATE_GROUP);
                    String linenum = matcher.group(LINENUM_GROUP);
                    String revision = matcher.group(REVISION_GROUP);
                    String text = matcher.group(TEXT_GROUP);

                    try {
                        VcsRevisionNumber revisionNumber = revisions.get(revision);
                        if (null == revisionNumber) {
                            revisionNumber = MksRevisionNumber.createRevision(revision);
                            revisions.put(revision, revisionNumber);
                        }
                        lineInfos.add(new MksFileAnnotation.LineInfo(parseDate(df, date), revisionNumber, author, null /* todo CPID*/));
                        lines.add(text);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (VcsException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.err.println("unexpected line [" + line + "]");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Date parseDate(DateFormat df, String date) throws ParseException {
        return df.parse(date);
    }

    public List<MksFileAnnotation.LineInfo> getLineInfos() {
        return lineInfos;
    }

    public List<String> getLines() {
        return lines;
    }
    public List<VcsRevisionNumber> getRevisions() {
        ArrayList<VcsRevisionNumber> numbers = new ArrayList<VcsRevisionNumber>();
        numbers.addAll(revisions.values());
        return numbers;

    }
}
