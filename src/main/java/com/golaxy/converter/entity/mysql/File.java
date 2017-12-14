package com.golaxy.converter.entity.mysql;

import java.util.Date;

public class File {
    private Integer id;

    private String md5;

    private String name;

    private String path;

    private String type;

    private Date uploadTime;

    private Integer convertStatus;

    private String convertLog;

    private Boolean convertResult;

    private Boolean noticeStatus;

    private String swfPath;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5 == null ? null : md5.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public Date getUploadTime() { return uploadTime; }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Integer getConvertStatus() {
        return convertStatus;
    }

    public void setConvertStatus(Integer convertStatus) {
        this.convertStatus = convertStatus;
    }

    public String getConvertLog() {
        return convertLog;
    }

    public void setConvertLog(String convertLog) {
        this.convertLog = convertLog;
    }

    public Boolean getConvertResult() {
        return convertResult;
    }

    public void setConvertResult(Boolean convertResult) {
        this.convertResult = convertResult;
    }

    public Boolean getNoticeStatus() {
        return noticeStatus;
    }

    public void setNoticeStatus(Boolean noticeStatus) {
        this.noticeStatus = noticeStatus;
    }

    public String getSwfPath() {
        return swfPath;
    }

    public void setSwfPath(String swfPath) {
        this.swfPath = swfPath;
    }
}