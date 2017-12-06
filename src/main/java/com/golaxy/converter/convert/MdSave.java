package com.golaxy.converter.convert;

import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.exception.ExistException;
import com.golaxy.converter.service.gitlab.IGitlabService;
import com.golaxy.converter.service.mysql.*;
import com.golaxy.converter.utils.ContextUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 转换结果存储相关操作
 */
public class MdSave {

    /**
     * 保存md到gitlab
     * @param articleName
     * @param userName
     * @param mdList
     * @param imgList
     */
    public static void gitlabSave(String articleName, String userName, List<ConverterResult> mdList, List<ConverterResult> imgList) {

        IGitlabService gitlabService = (IGitlabService) ContextUtil.getBean("gitlabService");

        //先用第一页试探是否已存在同名文件，如果是，则存储文件夹改为 文件名（i）的形式
        String articleSrcName = articleName;
        for(int i=0; i<mdList.size(); i++) {
            String mdPath = mdList.get(i).getAbsolutePath();
            String mdName = mdList.get(i).getName();
            if (i == 0) {
                int j = 1;
                while (true) {
                    try {
                        gitlabService.gitlabSaveNone(articleName, mdName, mdPath, userName);
                    } catch (ExistException e) {
                        articleName = articleSrcName + "(" + j++ + ")";
                        continue;
                    }
                    break;
                }
            } else {
                try {
                    gitlabService.gitlabSaveNone(articleName, mdName, mdPath, userName);
                } catch (ExistException e) {
                    e.printStackTrace();
                }
            }
        }

        for (ConverterResult img: imgList) {
            String imgName = img.getName();
            String imgPath = img.getAbsolutePath();
            try {
                gitlabService.gitlabSave(articleName, imgName, imgPath, userName, false);
            } catch (ExistException e) {
                e.printStackTrace();
            }
        }

        Iterator<ConverterResult> it = mdList.iterator();
        while (it.hasNext()) {
            ConverterResult md = it.next();
            String mdName = md.getName();
            String mdPath = md.getAbsolutePath();
            String mdGitPath = null;
            try {
                mdGitPath = gitlabService.gitlabSave(articleName, mdName, mdPath, userName, true);
            } catch (ExistException e) {
                e.printStackTrace();
            }
            String gitlabRawUrl = GlobalVars.gitlabRawBaseUrl + mdGitPath;
            md.setUrl(gitlabRawUrl);
            md.setGitPath(mdGitPath);
        }
    }

    /**
     * 更新MySQL转换状态及结果相关字段
     * @param fileMd5
     * @param status
     * @param errMsg
     */
    public static void mysqlUpdateConvertStatus(String fileMd5, int status, String errMsg) {

        IFileService fileService = (IFileService) ContextUtil.getBean("fileService");

        fileService.convertStatusUpdate(fileMd5, status, errMsg);
    }

    /**
     * 更新MySQL是否已通知用户字段
     * @param fileMd5
     * @param status
     */
    public static void mysqlUpdateNotice(String fileMd5, boolean status) {

        IFileService fileService = (IFileService) ContextUtil.getBean("fileService");

        fileService.noticeStatusUpdate(fileMd5, status);
    }

    /**
     * 保存本地存储路径到MySQL
     * @param fileMd5
     * @param imgList
     * @param mdList
     */
    public static void mysqlSaveLocal(String fileMd5, List<ConverterResult> imgList, List<ConverterResult> mdList) {

        IMdLocalService mdLocalService = (IMdLocalService) ContextUtil.getBean("mdLocalService");

        for (ConverterResult img: imgList) {
            String name = img.getName();
            String path = img.getRelativePath();
            Short index = img.getPage();

            mdLocalService.MdLocalAdd(fileMd5, 0, name, path, index);
        }

        for (ConverterResult md: mdList) {
            String name = md.getName();
            String path = md.getRelativePath();
            Short page = md.getPage();

            mdLocalService.MdLocalAdd(fileMd5, 1, name, path, page);
        }
    }

    /**
     * 保存gitlab存储路径到MySQL
     * @param articleUid
     * @param fileMd5
     * @param articleName
     * @param userName
     * @param userSource
     * @param cateId
     * @param mdList
     */
    public static void mysqlSaveRemote(String articleUid, String fileMd5, String articleName, String userName,
                                 String userSource, Integer cateId, List<ConverterResult> mdList) {

        ICategoryService categoryService = (ICategoryService) ContextUtil.getBean("categoryService");
        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");
        IMdService mdService = (IMdService) ContextUtil.getBean("mdService");

        //判断cateId是否为0，如果为0需要到MySQL中查找“未分类”ID
        if (cateId == 0) {
            cateId = categoryService.getCategoryId("未分类", 0, false);
            if (cateId == 0) {
                // 未找到，则插入一条“未分类”记录
                cateId = categoryService.CategoryAdd("未分类", 0, false);
            }
        }

        int uploadUserSourceId;
        switch (userSource) {
            case "tatfook":
                uploadUserSourceId = 0;
                break;
            case "keepwork":
                uploadUserSourceId = 1;
                break;
            default:
                uploadUserSourceId = 0;
                break;
        }

        int articleId = articleService.articleAdd(articleUid, fileMd5, articleName,
                uploadUserSourceId, userName, cateId, mdList.size());
        if (articleId > 0) {
            Iterator<ConverterResult> it = mdList.iterator();
            while (it.hasNext()) {
                ConverterResult md = it.next();
                String mdTitle = md.getName();
                String mdGitPath = md.getGitPath();
                int page = md.getPage();

                mdService.MdAdd(articleId, articleUid, mdTitle, mdGitPath, page);
            }
        }
    }

    public static void esSave() {

    }

    public static void mysqlUpdateEsId() {

    }

}
