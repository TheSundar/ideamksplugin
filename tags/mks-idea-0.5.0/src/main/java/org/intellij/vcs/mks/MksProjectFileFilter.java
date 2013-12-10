package org.intellij.vcs.mks;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class MksProjectFileFilter extends FileFilter {

    public MksProjectFileFilter() {
    }

    public String getDescription() {
        return "Source Integrity Project (*.pj)";
    }

    public boolean accept(File file) {
        if (file.isDirectory())
            return true;
        String extension = getExtension(file);
        if (extension != null)
            return "pj".equalsIgnoreCase(extension);
        else
            return false;
    }

    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1)
            ext = s.substring(i + 1).toLowerCase();
        return ext;
    }

    private static final String MKS_PROJECT_FILE_EXT = "pj";
    private static final String MKS_PROJECT_FILE_DESC = "Source Integrity Project (*.pj)";
}
