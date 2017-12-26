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
	
	public int mdAdd(int articleId, String articleUid, String mdTitle, String mdUrl, int page) {
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

	@Override
	public boolean indexStatusUpdate(int id, String mdUid) {

	    Md md = new Md();
	    md.setId(id);
	    if (mdUid!=null && !mdUid.equals("")) {
			md.setMdUid(mdUid);
			md.setIndexStatus(true);
		}

        int rows = mdDao.updateByPrimaryKeySelective(md);

        return rows>0? true : false;
    }

}
