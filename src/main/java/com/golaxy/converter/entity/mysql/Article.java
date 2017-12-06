package com.golaxy.converter.entity.mysql;

import java.util.Date;

public class Article {
    private Integer id;

    private String articleUid;

    private String fileMd5;

    private String title;

    private String url;

    private Integer parentCateId;

    private Boolean artEditStatus;

    private Boolean artCheckStatus;

    private Boolean status;

    private Integer listOrder;

    private Date createTime;

    private Date updateTime;

    private Integer uploadUserSource;

    private String author;

    private String lastUpdateUsername;

    private Integer type;
    
    private Integer totalPage;

    private Boolean publicStatus;
    
    private Boolean indexStatus;

    private String content;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getArticleUid() {
        return articleUid;
    }

    public void setArticleUid(String articleUid) {
        this.articleUid = articleUid == null ? null : articleUid.trim();
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Integer getParentCateId() {
        return parentCateId;
    }

    public void setParentCateId(Integer parentCateId) {
        this.parentCateId = parentCateId;
    }

    public Boolean getArtEditStatus() {
        return artEditStatus;
    }

    public void setArtEditStatus(Boolean artEditStatus) {
        this.artEditStatus = artEditStatus;
    }

    public Boolean getArtCheckStatus() {
        return artCheckStatus;
    }

    public void setArtCheckStatus(Boolean artCheckStatus) {
        this.artCheckStatus = artCheckStatus;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getListOrder() {
        return listOrder;
    }

    public void setListOrder(Integer listOrder) {
        this.listOrder = listOrder;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getUploadUserSource() {
        return uploadUserSource;
    }

    public void setUploadUserSource(Integer uploadUserSource) {
        this.uploadUserSource = uploadUserSource;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author == null ? null : author.trim();
    }

    public String getLastUpdateUsername() {
        return lastUpdateUsername;
    }

    public void setLastUpdateUsername(String lastUpdateUsername) {
        this.lastUpdateUsername = lastUpdateUsername == null ? null : lastUpdateUsername.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
    
    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Boolean getPublicStatus() {
        return publicStatus;
    }

    public void setPublicStatus(Boolean publicStatus) {
        this.publicStatus = publicStatus;
    }

	public Boolean getIndexStatus() {
		return indexStatus;
	}

	public void setIndexStatus(Boolean indexStatus) {
		this.indexStatus = indexStatus;
	}

	public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }
}