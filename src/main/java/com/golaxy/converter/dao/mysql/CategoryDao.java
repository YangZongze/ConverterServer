package com.golaxy.converter.dao.mysql;


import com.golaxy.converter.entity.mysql.Category;

public interface CategoryDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);
    
    Category selectByName(String name, Integer parentId, Boolean status);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);
}