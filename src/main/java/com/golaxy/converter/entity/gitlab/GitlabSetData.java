package com.golaxy.converter.entity.gitlab;

public class GitlabSetData {
	
	private String branch;
	private String commit_message;
	private String content;
	private String encoding;
	private String path;
	
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getCommit_message() {
		return commit_message;
	}
	public void setCommit_message(String commit_message) {
		this.commit_message = commit_message;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

}
