package org.intellij.vcs.mks;


class MksCapabilities {

    MksCapabilities() {
    }

    public boolean isFileMovingSupported() {
        return false;
    }

    public boolean isFileRenamingSupported() {
        return true;
    }

    public boolean isDirectoryMovingSupported() {
        return false;
    }

    public boolean isDirectoryRenamingSupported() {
        return false;
    }

    public boolean isTransactionSupported() {
        return false;
    }
}
