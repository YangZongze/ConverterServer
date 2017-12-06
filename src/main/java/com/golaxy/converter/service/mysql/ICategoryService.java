package com.golaxy.converter.service.mysql;

public interface ICategoryService {

	public int CategoryAdd(String name, Integer parentId, Boolean status);
	
	public int getCategoryId(String name, Integer parentId, Boolean status);
}
