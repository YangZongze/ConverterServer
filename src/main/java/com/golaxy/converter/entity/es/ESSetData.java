package com.golaxy.converter.entity.es;

public class ESSetData {
	
	private String article_uid;
	private String title;
	private String content;
	private String path;
	private int page;
	private int totalpage;
	private boolean public_status;
	private int source;
	private String author;
	private String publish_time;
	
	public String getArticle_uid() {
		return article_uid;
	}
	public void setArticle_uid(String article_uid) {
		this.article_uid = article_uid;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	
	public int getTotalpage() {
		return totalpage;
	}
	public void setTotalpage(int totalpage) {
		this.totalpage = totalpage;
	}
	
	public boolean getPublic_status() {
		return public_status;
	}
	public void setPublic_status(boolean public_status) {
		this.public_status = public_status;
	}
	
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getPublish_time() {
		return publish_time;
	}
	public void setPublish_time(String publish_time) {
		this.publish_time = publish_time;
	}

}
