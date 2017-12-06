package com.golaxy.converter.service.mysql.impl;

import java.util.Date;

import javax.annotation.Resource;

import com.golaxy.converter.dao.mysql.CategoryDao;
import com.golaxy.converter.entity.mysql.Category;
import com.golaxy.converter.service.mysql.ICategoryService;
import org.springframework.stereotype.Service;


@Service("categoryService")
public class CategoryServiceImpl implements ICategoryService {

	@Resource
	private CategoryDao categoryDao;
	
	@Override
	public int CategoryAdd(String name, Integer parentId, Boolean status) {
		Category category = new Category();
		category.setName(name);
		category.setParentId(parentId);
		category.setStatus(status);
		category.setCreateTime(new Date());
		
		return this.categoryDao.insertSelective(category);
	}
	
	@Override
	public int getCategoryId(String name, Integer parentId, Boolean status) {
		
		Category category = this.categoryDao.selectByName(name, parentId, status);
		
		return category.getId();
	}

}
