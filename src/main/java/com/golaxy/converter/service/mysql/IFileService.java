package com.golaxy.converter.service.mysql;

import com.golaxy.converter.entity.mysql.File;

import java.util.List;


public interface IFileService {

	public int fileAdd(String md5, String name, String path, String type);

	public int fileSearch(String md5);

    public File getFileByUid(String md5);

    public boolean convertStatusUpdate(String md5, Integer status, String errMsg);

    public boolean noticeStatusUpdate(String md5, boolean status);

    public boolean swfPathUpdate(String md5, boolean isRunning, String swfPath);

    public String getFileType(String md5);

    public boolean getSwfConvertIsRunning(String md5);

}
