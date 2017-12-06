package com.golaxy.converter.dao.mysql;


import com.golaxy.converter.entity.mysql.MdLocal;

import java.util.List;

public interface MdLocalDao {
    int deleteByPrimaryKey(Integer id);

    int insert(MdLocal record);

    int insertSelective(MdLocal record);

    MdLocal selectByPrimaryKey(Integer id);

    List<MdLocal> selectByFileMd5(String md5);

    int updateByPrimaryKeySelective(MdLocal record);

    int updateByPrimaryKey(MdLocal record);
}