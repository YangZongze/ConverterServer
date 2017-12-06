package com.golaxy.converter.entity.frontend;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 格式转换结果
 */
public class ConverterResult {

    private String name;
    /** 本地存储绝对路径 **/
    private String absolutePath;
    /** 本地存储相对路径 **/
    private String relativePath;
    /** md页码 **/
    private Short page;
    /** 本机访问url或gitlab访问url **/
    private String url;
    /** gitlab存储路径 **/
    private String gitPath;

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
}
