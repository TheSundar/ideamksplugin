package org.intellij.vcs.mks;

import java.util.List;

/**
 * @author Thibaut Fagart
 */
public class MksChangePackage {
    private String id;
    private String owner;
    private String state;
    private String summary;

    private List<MksChangePackageEntry> entries;
    public final String server;

    public MksChangePackage(final String server, String id, String owner, String state, String summary) {
        this.server = server;
        this.summary = summary;
        this.id = id;
        this.owner = owner;
        this.state = state;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setEntries(List<MksChangePackageEntry> entries) {
        this.entries = entries;
    }

    public List<MksChangePackageEntry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return "MksChangePackage{id='" + id + "\'}";
    }
}
