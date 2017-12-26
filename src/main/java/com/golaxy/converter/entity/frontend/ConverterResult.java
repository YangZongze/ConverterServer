package com.golaxy.converter.entity.frontend;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 格式转换结果
 */
public class ConverterResult implements Cloneable {

    private String name;
    /** 本地存储绝对路径 **/
    private String absolutePath;
    /** 本地存储相对路径 **/
    private String relativePath;
    /** md页码 **/
    private Short page;
    /** md页码的markdown表现形式 **/
    private String pageMd;
    /** 本机访问url或gitlab访问url **/
    private String url;
    /** gitlab存储路径 **/
    private String gitPath;
    /** 数据库md表主键 **/
    private int mdId;
    /** 上传用户名 **/
    private String uploadUserName;
    /** 上传用户来源 **/
    private int uploadUserSourceId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public Short getPage() {
        return page;
    }

    public void setPage(Short page) {
        this.page = page;
    }

    public String getPageMd() {
        return pageMd;
    }

    public void setPageMd(String pageMd) {
        this.pageMd = pageMd;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGitPath() {
        return gitPath;
    }

    public void setGitPath(String gitPath) {
        this.gitPath = gitPath;
    }

    public int getMdId() {
        return mdId;
    }

    public void setMdId(int mdId) {
        this.mdId = mdId;
    }

    public String getUploadUserName() {
        return uploadUserName;
    }

    public void setUploadUserName(String uploadUserName) {
        this.uploadUserName = uploadUserName;
    }

    public int getUploadUserSourceId() {
        return uploadUserSourceId;
    }

    public void setUploadUserSourceId(int uploadUserSourceId) {
        this.uploadUserSourceId = uploadUserSourceId;
    }

    @Override
    public ConverterResult clone() {
        ConverterResult clone = null;
        try {
            clone = (ConverterResult) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return clone;
    }

}
