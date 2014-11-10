package org.intellij.vcs.mks.history;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.annotate.AnnotationSourceSwitcher;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspectAdapter;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsFileUtil;
import com.intellij.vcsUtil.VcsUtil;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.MksVcs;
import org.intellij.vcs.mks.model.MksChangePackage;
import org.intellij.vcs.mks.model.MksMemberRevisionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class MksFileAnnotation extends FileAnnotation {
    private final List<String> lines;
    private final Project project;
    private List<VcsRevisionNumber> revisions;
    private ArrayList<VcsFileRevision> fileRevisions;
    private Map<VcsRevisionNumber, VcsFileRevision> fileRevisionMap = new HashMap<VcsRevisionNumber, VcsFileRevision>();
    private String myAnnotatedContent;

    public static class LineInfo {
        private final Date myDate;
        private final VcsRevisionNumber myRevision;
        private final String myAuthor;
        private final MksChangePackage myChangePackage;
        private static final MksChangePackage NONE = new MksChangePackage("", "", "", "", "");

        public LineInfo(Date date, VcsRevisionNumber revision, final String author, @Nullable MksChangePackage changePackage) {
            myDate = date;
            myRevision = revision;
            myAuthor = author;
            myChangePackage = changePackage == null ? NONE : changePackage;

        }

        public Date getDate() {
            return myDate;
        }


        public String getAuthor() {
            return myAuthor;
        }
    }
    private static abstract class MksAnnotationAspect extends LineAnnotationAspectAdapter {
        public MksAnnotationAspect(String id, boolean showByDefault) {
            super(id, showByDefault);
        }

        @Override
        protected void showAffectedPaths(int lineNum) {
            // do nothing
        }
    }

    private VirtualFile file;
    private List<LineInfo> lineInfos ;
    public MksFileAnnotation(Project project, VirtualFile file, @NotNull List<LineInfo> lineInfos, List<String> lines, List<VcsRevisionNumber> revisions, ArrayList<VcsFileRevision> fileRevisions) {
        super(project);
        this.project = project;
        this.file = file;
        this.lineInfos = lineInfos;
        this.lines = lines;
        this.revisions = revisions;
        this.fileRevisions = fileRevisions;
        for (VcsFileRevision fileRevision : fileRevisions) {
            fileRevisionMap.put(fileRevision.getRevisionNumber(), fileRevision);
        }
    }

    //    "author,cpid,date,labels,linenum,revision,text"
    @Override
    public void dispose() {

    }

    @Override
    public LineAnnotationAspect[] getAspects() {
        return new LineAnnotationAspect[]{
                new MksAnnotationAspect(LineAnnotationAspect.AUTHOR, false) {
                    @Override
                    public String getValue(int line) {

                        return line < lineInfos.size()?lineInfos.get(line).getAuthor() :"";
                    }
                },
                new MksAnnotationAspect(LineAnnotationAspect.DATE, false) {
                    SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                    @Override
                    public String getValue(int line) {
                        return line < lineInfos.size()?format.format(lineInfos.get(line).getDate()):"";
                    }
                },
                new MksAnnotationAspect(LineAnnotationAspect.REVISION, true) {
                    @Override
                    public String getValue(int line) {
                        return line < lineInfos.size()? lineInfos.get(line).myRevision.asString():"";
                    }
                },
                new MksAnnotationAspect("Change Package", false) {
                    @Override
                    public String getValue(int line) {
                        return line < lineInfos.size()?lineInfos.get(line).myChangePackage.getId():"";
                    }
                }
        };
    }

    @Nullable
    @Override
    public String getToolTip(int lineNumber) {
        VcsFileRevision lineFileRevision = fileRevisionMap.get(getLineRevisionNumber(lineNumber));
        return "Revision " + lineFileRevision.getRevisionNumber().asString() + ": " + lineFileRevision.getCommitMessage();
    }

    @Override
    public String getAnnotatedContent() {
        if (myAnnotatedContent == null) {
            StringWriter stringWriter = new StringWriter();
            BufferedWriter writer = new BufferedWriter(stringWriter);
            try {
                boolean first = true;
                for (String line : lines) {
                    if (!first) {
                        writer.write("\n");
                        first = false;
                    }
                    writer.write(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            myAnnotatedContent = stringWriter.toString();
        }
        return myAnnotatedContent;
    }

    @Nullable
    @Override
    public VcsRevisionNumber getLineRevisionNumber(int lineNumber) {
        return (lineNumber < lineInfos.size()) ? lineInfos.get(lineNumber).myRevision: null;
    }

    @Nullable
    @Override
    public Date getLineDate(int lineNumber) {
        return (lineNumber < lineInfos.size()) ?  lineInfos.get(lineNumber).getDate() : null;
    }

    @Nullable
    @Override
    public VcsRevisionNumber originalRevision(int lineNumber) {
        return getLineRevisionNumber(lineNumber);
    }

    @Nullable
    @Override
    public VcsRevisionNumber getCurrentRevision() {
        return fileRevisions.get(0).getRevisionNumber() ; // todo : not the current one, get(0) is the most recent affecting the file
    }

    @Nullable
    @Override
    public List<VcsFileRevision> getRevisions() {
        return fileRevisions;
    }

    @Override
    public boolean revisionsNotEmpty() {
        return !fileRevisions.isEmpty();
    }

    @Nullable
    @Override
    public AnnotationSourceSwitcher getAnnotationSourceSwitcher() {
        return null;
    }

    @Override
    public int getLineCount() {
        return lineInfos.size();
    }

    @Override
    public VirtualFile getFile() {
        return file;
    }
}
