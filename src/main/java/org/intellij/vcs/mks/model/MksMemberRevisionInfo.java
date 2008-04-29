package org.intellij.vcs.mks.model;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;

import java.util.Date;

public class MksMemberRevisionInfo {
	private String cpid;
	private String author;
	private String description;
	private Date date;
	private VcsRevisionNumber revision;

	public void setCPID(String cpid) {
		this.cpid = cpid;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setRevision(VcsRevisionNumber revision) {
		this.revision = revision;
	}

	public String getAuthor() {
		return author;
	}

	public String getCpid() {
		return cpid;
	}

	public Date getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public VcsRevisionNumber getRevision() {
		return revision;
	}
}
