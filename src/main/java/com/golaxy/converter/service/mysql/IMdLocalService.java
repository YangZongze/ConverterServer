package com.golaxy.converter.service.mysql;

import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.entity.mysql.MdLocal;

import java.util.List;


public interface IMdLocalService {

	public int MdLocalAdd(String fileMd5, Integer type, String name, String path, Short page);

	public List<MdLocal> getResultByMd5(String fileMd5);
	
}
