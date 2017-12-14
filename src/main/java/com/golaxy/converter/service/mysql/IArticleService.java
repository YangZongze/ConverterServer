package com.golaxy.converter.service.mysql;


import com.golaxy.converter.entity.mysql.Article;

public interface IArticleService {

	public int articleAdd(String articleUid, String fileMd5, String articleName, Integer uploadUserSourceId,
                          String uploadUserName, Integer cateId, Integer totalPage);

    public Article getArticleById(int articleId);

	public Article getArticleByUid(String articleUid);

	public int articleSearch(String md5, String userName);
}
