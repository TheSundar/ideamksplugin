package org.intellij.vcs.mks;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class ChangedResourcesTableModel extends AbstractTableModel {
    ArrayList<VirtualFile> files = new ArrayList<VirtualFile>();
    ArrayList<FileStatus> statuses = new ArrayList<FileStatus>();
    private static final int STATUS = 1;
    private static final int FILE = 0;

    protected VirtualFile getVirtualFile(int row) {
        return files.get(row);
    }

    public void setFiles(Map<VirtualFile, FileStatus> fileStatuses) {
        files = new ArrayList<VirtualFile>(fileStatuses.size());
        statuses = new ArrayList<FileStatus>(fileStatuses.size());

        for (VirtualFile file : fileStatuses.keySet()) {
            if (!(fileStatuses.get(file)).equals(FileStatus.NOT_CHANGED)) {
                files.add(file);
            }
        }
        Collections.sort(files, new Comparator<VirtualFile>() {
            public int compare(VirtualFile virtualFile, VirtualFile virtualFile1) {
                return virtualFile.getPath().compareTo(virtualFile1.getPath());
            }
        });
        for (VirtualFile file : files) {
            statuses.add(fileStatuses.get(file));
        }
        fireTableDataChanged();
    }

    public int getRowCount() {
        return files.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int row, int column) {
        if (row >= 0 && row < files.size()) {
            switch (column) {
                case FILE:
                    return files.get(row).getPath();
                case STATUS:
                    return statuses.get(row);
                default:
                    return null;
            }
        } else return null;
    }
}
