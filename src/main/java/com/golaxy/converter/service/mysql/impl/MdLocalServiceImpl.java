package com.golaxy.converter.service.mysql.impl;

import com.golaxy.converter.convert.GlobalVars;
import com.golaxy.converter.dao.mysql.MdLocalDao;
import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.entity.mysql.MdLocal;
import com.golaxy.converter.service.mysql.IMdLocalService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service("mdLocalService")
public class MdLocalServiceImpl implements IMdLocalService {

	@Resource
	private MdLocalDao mdLocalDao;
	
	@Override
	public int MdLocalAdd(String fileMd5, Integer type, String name, String path, Short page) {

		MdLocal md = new MdLocal();
		md.setFileMd5(fileMd5);
		md.setType(type);
		md.setName(name);
		md.setPath(path);
		md.setPage(page);
		
		this.mdLocalDao.insertSelective(md);
		
		return md.getId();
	}
	
	@Override
	public List<MdLocal> getResultByMd5(String fileMd5) {

        return mdLocalDao.selectByFileMd5(fileMd5);
	}

	@Override
	public List<MdLocal> getMdPathByMd5(String fileMd5, int imgOrMd) {

		return mdLocalDao.selectByType(fileMd5, imgOrMd);
	}

}
