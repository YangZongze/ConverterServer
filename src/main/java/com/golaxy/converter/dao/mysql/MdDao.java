package com.golaxy.converter.dao.mysql;

import com.golaxy.converter.entity.mysql.Md;

import java.util.List;

public interface MdDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Md record);

    int insertSelective(Md record);

    Md selectByPrimaryKey(Integer id);
    
    List<Md> selectByArticleUid(String articleUid);

    int updateByPrimaryKeySelective(Md record);

    int updateByPrimaryKey(Md record);
}