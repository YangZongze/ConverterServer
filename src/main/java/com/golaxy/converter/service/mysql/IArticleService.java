package com.golaxy.converter.service.mysql;


import com.golaxy.converter.entity.mysql.Article;
import org.apache.ibatis.javassist.NotFoundException;

public interface IArticleService {

	public int articleAdd(String articleUid, String fileMd5, String articleName, Integer uploadUserSourceId,
                          String uploadUserName, Integer cateId, Integer totalPage);

    public void articleDel(String articleUid);

    public Article getArticleById(int articleId);

	public Article getArticleByUid(String articleUid);

	public int articleSearch(String md5, String userName);

	public boolean indexStatusUpdate(String articleUid, boolean status);

    public boolean getIndexStatus(String articleUid) throws NotFoundException;

    public Article totalPageUpdate(String articleUid, int totalPage);

}
