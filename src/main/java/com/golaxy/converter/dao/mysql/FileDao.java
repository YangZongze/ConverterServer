package com.golaxy.converter.dao.mysql;

import com.golaxy.converter.entity.mysql.File;

public interface FileDao {
    int deleteByPrimaryKey(Integer id);

    int insert(File record);

    int insertSelective(File record);

    File selectByPrimaryKey(Integer id);
    
    File selectByUniqueKey(String uid);

    int updateByPrimaryKeySelective(File record);

    int updateByPrimaryKey(File record);

    int updateByUniqueKeySelective(File record);
}