package com.golaxy.converter.entity.mysql;

public class Md {
    private Integer id;

    private Integer articleId;
    
    private String articleUid;

    private String mdUid;

    private String mdTitle;

    private String mdUrl;

    private Integer page;

    private Boolean indexStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }
    
    public String getArticleUid() {
        return articleUid;
    }

    public void setArticleUid(String articleUid) {
        this.articleUid = articleUid == null ? null : articleUid.trim();
    }
    
    public String getMdUid() {
        return mdUid;
    }

    public void setMdUid(String mdUid) {
        this.mdUid = mdUid == null ? null : mdUid.trim();
    }

    public String getMdTitle() {
        return mdTitle;
    }

    public void setMdTitle(String mdTitle) {
        this.mdTitle = mdTitle == null ? null : mdTitle.trim();
    }

    public String getMdUrl() {
        return mdUrl;
    }

    public void setMdUrl(String mdUrl) {
        this.mdUrl = mdUrl == null ? null : mdUrl.trim();
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Boolean getIndexStatus() {
        return indexStatus;
    }

    public void setIndexStatus(Boolean indexStatus) {
        this.indexStatus = indexStatus;
    }
}