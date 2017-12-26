package com.golaxy.converter.dao.mysql;

import com.golaxy.converter.entity.mysql.Article;

public interface ArticleDao {
    int deleteByPrimaryKey(Integer id);

    int deleteByUniqueKey(String uid);

    int insert(Article record);

    int insertSelective(Article record);

    Article selectByPrimaryKey(Integer id);
    
    Article selectByUniqueKey(String uid);

    Article selectByFileMd5(String md5);

    Article selectByUserMd5(String md5, String usrName);

    int updateByPrimaryKeySelective(Article record);

    int updateByPrimaryKeyWithBLOBs(Article record);

    int updateByPrimaryKey(Article record);
    
    int updateByUniqueKeySelective(Article record);
}