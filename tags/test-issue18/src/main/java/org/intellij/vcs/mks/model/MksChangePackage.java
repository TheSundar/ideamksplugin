package org.intellij.vcs.mks.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Embodies a Mks change package, we try to tie it to an IDEA changelist when possible. <br/>
 * Two changePackages are considered equal it they are from the same server and have the same id.
 *
 * @see org.intellij.vcs.mks.MksChangeListAdapter
 * @author Thibaut Fagart
 */
public class MksChangePackage {
	@NotNull
	private String id;
	@NotNull
    private String owner;
    private String state;
	@NotNull
    private String summary;
	@NotNull
	public final String server;
	private List<MksChangePackageEntry> entries;

	public MksChangePackage(@NotNull final String server, @NotNull String id, @NotNull String owner,
							@NotNull String state, String summary) {
        this.server = server;
        this.summary = summary;
        this.id = id;
        this.owner = owner;
        this.state = state;
    }

    @NotNull
	public String getSummary() {
        return summary;
    }

	@NotNull
	public String getId() {
        return id;
    }

	@NotNull
	public String getOwner() {
        return owner;
    }

	public String getState() {
        return state;
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

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MksChangePackage that = (MksChangePackage) o;

		return id.equals(that.id) && server.equals(that.server);

	}

	public int hashCode() {
		int result;
		result = id.hashCode();
		result = 31 * result + server.hashCode();
		return result;
	}
}
