package com.golaxy.converter.entity.gitlab;

public class GitlabGetData {
	
	private String blob_id;
	private String commit_id;
	private String content;
	private String encoding;
	private String file_name;
	private String file_path;
	private String last_commit_id;
	private String ref;
	private int size;
	
	public String getBlob_id() {
		return blob_id;
	}
	public void setBlob_id(String blob_id) {
		this.blob_id = blob_id;
	}
	public String getCommit_id() {
		return commit_id;
	}
	public void setCommit_id(String commit_id) {
		this.commit_id = commit_id;
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
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public String getFile_path() {
		return file_path;
	}
	public void setFile_path(String file_path) {
		this.file_path = file_path;
	}
	public String getLast_commit_id() {
		return last_commit_id;
	}
	public void setLast_commit_id(String last_commit_id) {
		this.last_commit_id = last_commit_id;
	}
	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
}
