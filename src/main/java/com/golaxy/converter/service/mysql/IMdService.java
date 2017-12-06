package com.golaxy.converter.service.mysql;

import com.golaxy.converter.entity.mysql.Md;

import java.util.List;


public interface IMdService {

	public int MdAdd(int articleId, String articleUid, String mdTitle, String mdUrl, int page);
	
	public List<Md> getMdByUid(String articleUid);
	
}
