package com.golaxy.converter.service.mysql;

import java.util.List;


public interface IFileService {

	public int fileAdd(String md5, String name, String path, String type);

	public int fileSearch(String md5);

    public boolean convertStatusUpdate(String md5, Integer status, String errMsg);

    public boolean noticeStatusUpdate(String md5, boolean status);

}
