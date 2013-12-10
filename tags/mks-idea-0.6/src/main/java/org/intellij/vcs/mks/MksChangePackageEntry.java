package org.intellij.vcs.mks;

/**
 * the 'member' attribute specifies a path relative to the 'project' owning directory
 *
 * @author Thibaut Fagart
 */
public class MksChangePackageEntry {
	private String type;
	private String member;
	private String revision;
	private String project;

	public MksChangePackageEntry(String type, String member, String revision, String project) {
		this.member = member;
		this.project = project;
		this.revision = revision;
		this.type = type;
	}


	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + member + "]";
	}

	public boolean isLocked() {
		return "Lock".equals(type);
	}
}
