package com.golaxy.converter.service.mysql.impl;

import java.util.List;

import javax.annotation.Resource;

import com.golaxy.converter.dao.mysql.MdDao;
import com.golaxy.converter.entity.mysql.Md;
import com.golaxy.converter.service.mysql.IMdService;
import org.springframework.stereotype.Service;


@Service("mdService")
public class MdServiceImpl implements IMdService {

	@Resource
	private MdDao mdDao;
	
	@Override
	public int MdAdd(int articleId, String articleUid, String mdTitle, String mdUrl, int page) {
		Md md = new Md();
		md.setArticleId(articleId);
		md.setArticleUid(articleUid);
		md.setMdTitle(mdTitle);
		md.setMdUrl(mdUrl);
		md.setPage(page);
		
		 this.mdDao.insertSelective(md);
		
		 return md.getId();
	}
	
	@Override
	public List<Md> getMdByUid(String articleUid) {
		
		return mdDao.selectByArticleUid(articleUid);
	}

}
