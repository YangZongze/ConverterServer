package com.golaxy.converter.service.mysql.impl;

import java.util.Date;

import javax.annotation.Resource;


import com.golaxy.converter.dao.mysql.ArticleDao;
import com.golaxy.converter.entity.mysql.Article;
import com.golaxy.converter.service.mysql.IArticleService;
import org.springframework.stereotype.Service;


@Service("articleService")
public class ArticleServiceImpl implements IArticleService {

	@Resource
	private ArticleDao articleDao;
	
	@Override
	public int articleAdd(String articleUid, String fileMd5, String articleName, Integer uploadUserSourceId,
			String uploadUserName, Integer cateId, Integer totalPage) {
		
		Article article = new Article();
		article.setArticleUid(articleUid);
		article.setFileMd5(fileMd5);
        article.setTitle(articleName);
        article.setParentCateId(cateId); //未分类
        article.setCreateTime(new Date());
        article.setUploadUserSource(uploadUserSourceId);
        article.setAuthor(uploadUserName);
        article.setTotalPage(totalPage);
        
        this.articleDao.insertSelective(article);
        
        return article.getId();
	}

    @Override
    public Article getArticleById(int articleId) {

	    return articleDao.selectByPrimaryKey(articleId);
    }

	@Override
	public Article getArticleByUid(String articleUid) {
		
		return articleDao.selectByUniqueKey(articleUid);
	}

	@Override
    public int articleSearch(String md5, String userName) {

        int id = -1;

        Article article = articleDao.selectByUserMd5(md5, userName);

        return article!=null ? article.getId() : id;
    }

}
