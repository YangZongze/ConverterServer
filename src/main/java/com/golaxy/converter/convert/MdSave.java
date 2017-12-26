package com.golaxy.converter.convert;

import com.golaxy.converter.entity.es.ESSetData;
import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.entity.mysql.Article;
import com.golaxy.converter.exception.ConvertFailException;
import com.golaxy.converter.exception.ExistException;
import com.golaxy.converter.service.es.IEsService;
import com.golaxy.converter.service.gitlab.IGitlabService;
import com.golaxy.converter.service.kafka.IKafkaService;
import com.golaxy.converter.service.mysql.*;
import com.golaxy.converter.utils.CommonUtils;
import com.golaxy.converter.utils.ContextUtil;
import com.golaxy.converter.utils.Office2Swf;
import org.apache.ibatis.javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 转换结果存储相关操作
 */
public class MdSave {

    private final static Logger logger = LoggerFactory.getLogger(MdSave.class);

    /**
     * 同步保存md和图片到gitlab
     * @param articleName
     * @param userName
     * @param mdList
     * @param imgList
     */
    public static void gitlabSave(String articleName, String userName,
                                  List<ConverterResult> mdList, List<ConverterResult> imgList) {

        if(gitlabSaveMd(articleName, userName, mdList))
            gitlabSaveImg(imgList);
    }

    /**
     * 同步保存md,异步保存图片
     */
    public static boolean gitlabSave(String articleName, String userName, String fileMd5,
                                  List<ConverterResult> mdList, List<ConverterResult> imgList) {
        boolean state = false;

        state = gitlabSaveMd(articleName, userName, mdList);
        if (state && imgList.size()>0)
            gitlabSaveImgAsyn(fileMd5, imgList, mdList);

        return state;
    }

    /**
     * 同步保存md到gitlab
     * @param articleName
     * @param userName
     * @param mdList
     */
    public static boolean gitlabSaveMd(String articleName, String userName, List<ConverterResult> mdList) {

        articleName = gitlabSaveMdStep1(articleName, userName, mdList);
        if (articleName != null)
            return gitlabSaveMdStep2(articleName, userName, mdList);
        else
            return false;
    }

    /**
     * 同步保存图片到gitlab
     * @param imgList
     */
    public static void gitlabSaveImg(List<ConverterResult> imgList) {
        IGitlabService gitlabService = (IGitlabService) ContextUtil.getBean("gitlabService");

        for (ConverterResult img: imgList) {
            String imgName = img.getName();
            String imgPath = img.getAbsolutePath();

            gitlabService.gitlabSaveImg(imgName, imgPath);
        }
    }

    /**
     * 异步保存图片到gitlab
     * @param fileMd5
     * @param imgList
     */
    public static void gitlabSaveImgAsyn(String fileMd5, List<ConverterResult> imgList, List<ConverterResult> mdList) {

        IKafkaService kafkaService = (IKafkaService) ContextUtil.getBean("kafkaService");

        Map<String, List<ConverterResult>> result = new HashMap<>();
        result.put("img", imgList);
        result.put("md", mdList);

        try {
            kafkaService.kafkaProduceGitlabUploadImg(fileMd5, result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("upload imgs write kafka error:" + e.getMessage());
        }
    }

    /**
     * 第一步,先创建好md第一页的空文件，把每一页页码的markdown保存在mdList中
     * @param articleName
     * @param userName
     * @param mdList
     * @return 保存的gitlab路径下的文件夹名字
     */
    public static String gitlabSaveMdStep1(String articleName, String userName, List<ConverterResult> mdList) {
        IGitlabService gitlabService = (IGitlabService) ContextUtil.getBean("gitlabService");

        //先用第一页试探是否已存在同名文件，如果是，则存储文件夹改为 文件名（i）的形式
        String articleSrcName = articleName;

        int i = 0;
        Iterator<ConverterResult> it = mdList.iterator();
        while (it.hasNext()) {
            ConverterResult md = it.next();
            String mdPath = md.getAbsolutePath();
            String mdName = md.getName();
            if (i == 0) {
                int j = 1;
                while (true) {
                    try {
                        String gitpath = gitlabService.gitlabSaveNone(articleName, mdName, mdPath, userName);
                        if (gitpath == null)
                            return null;
                        String keepworkPath = gitpath.replace(".md", "").replace(GlobalVars.keepworkUserProj, "");
                        md.setPageMd("[第"+(i+1)+"页]("+keepworkPath+") ");
                    } catch (ExistException e) {
                        articleName = articleSrcName + "（" + j++ + "）";
                        continue;
                    }
                    break;
                }
            } else {
                String gitpath = gitlabService.getGitlabSavePath(articleName, mdName);
                String keepworkPath = gitpath.replace(".md", "").replace(GlobalVars.keepworkUserProj, "");
                md.setPageMd("[第"+(i+1)+"页]("+keepworkPath+") ");
            }
            i++;
        }
        return articleName;
    }

    /**
     * 第二步，提交真正的md文本
     * @param articleName
     * @param userName
     * @param mdList
     */
    public static boolean gitlabSaveMdStep2(String articleName, String userName, List<ConverterResult> mdList) {
        IGitlabService gitlabService = (IGitlabService) ContextUtil.getBean("gitlabService");
        boolean state = true;

        Iterator<ConverterResult> it = mdList.iterator();
        int i = 1;
        while (it.hasNext()) {
            ConverterResult md = it.next();
            String mdName = md.getName();
            String mdPath = md.getAbsolutePath();
            String mdGitPath = null;
            try {
                String pageStr = "\n---\n";
                int pageNum = mdList.size();
                for (int j=1; j<=pageNum; j++) {
                    if (i == j) {
                        pageStr += "第"+i+"页 ";
                        continue;
                    }
                    pageStr += mdList.get(j-1).getPageMd();
                }
                String fileContent = "";
                if (pageNum > 1)
                    fileContent = CommonUtils.read(mdPath, "UTF-8") + pageStr;
                else
                    fileContent = CommonUtils.read(mdPath, "UTF-8");
                if (i == 1) {
                    mdGitPath = gitlabService.gitlabSaveContent(articleName, mdName, fileContent, userName, true);
                    if (mdGitPath == null) {
                        return false;
                    }
                } else {
                    try {
                        mdGitPath = gitlabService.gitlabSaveContent(articleName, mdName, fileContent, userName, false);
                    } catch (Exception e) {
                        mdGitPath = gitlabService.gitlabSaveContent(articleName, mdName, fileContent, userName, true);
                    }
                }
                if (pageNum > 1)
                    CommonUtils.write(mdPath, fileContent, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mdGitPath != null) {
                String gitlabRawUrl = GlobalVars.gitlabRawBaseUrl + mdGitPath;
                md.setUrl(gitlabRawUrl);
                md.setGitPath(mdGitPath);
            } else {
                state = false;
            }
            i++;
        }
        return state;
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

    public static boolean mysqlSaveRemoteArticle(String articleUid, String fileMd5, String articleName,
                                                 String userName, String userSource, Integer cateId) {

        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");
        ICategoryService categoryService = (ICategoryService) ContextUtil.getBean("categoryService");

        Article article = articleService.getArticleByUid(articleUid);
        if (article == null) {
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
                    uploadUserSourceId, userName, cateId, null);
            if (articleId > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void mysqlDeleteArticle(String articleUid) {
        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");

        articleService.articleDel(articleUid);
    }

    /**
     * 保存gitlab存储路径到MySQL
     * @param articleUid
     * @param userName
     * @param mdList
     */
    public static void mysqlSaveRemoteMd(String articleUid, String userName, List<ConverterResult> mdList) {

        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");
        IMdService mdService = (IMdService) ContextUtil.getBean("mdService");

        Article article = articleService.totalPageUpdate(articleUid, mdList.size());
        if (article != null) {
            Iterator<ConverterResult> it = mdList.iterator();
            while (it.hasNext()) {
                ConverterResult md = it.next();
                String mdTitle = md.getName();
                String mdGitPath = md.getGitPath();
                int page = md.getPage();

                int mdId = mdService.mdAdd(article.getId(), articleUid, mdTitle, mdGitPath, page);
                md.setMdId(mdId);
                md.setUploadUserName(userName);
                md.setUploadUserSourceId(article.getUploadUserSource());
            }
        }
    }

    /**
     * 保存gitlab存储路径到MySQL
     * @param articleUid
     * @param userName
     * @param mdList
     */
    public static void mysqlSaveRemote(String articleUid, String md5, String articleName,
                                       String userName, String userSource, int cateId, List<ConverterResult> mdList) {

        if(mysqlSaveRemoteArticle(articleUid, md5, articleName, userName, userSource, cateId)) {
            mysqlSaveRemoteMd(articleUid, userName, mdList);
        }

    }

//    /**
//     * 保存gitlab存储路径到MySQL
//     * @param articleUid
//     * @param userName
//     * @param mdList
//     */
//    public static void mysqlSaveRemote(String articleUid, String md5, String articleName,
//                                       String userName, String userSource, int cateId, List<ConverterResult> mdList) {
//
//
//        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");
//        IMdService mdService = (IMdService) ContextUtil.getBean("mdService");
//
//        Article article = articleService.totalPageUpdate(articleUid, mdList.size());
//        if (article != null) {
//            Iterator<ConverterResult> it = mdList.iterator();
//            while (it.hasNext()) {
//                ConverterResult md = it.next();
//                String mdTitle = md.getName();
//                String mdGitPath = md.getGitPath();
//                int page = md.getPage();
//
//                int mdId = mdService.mdAdd(article.getId(), articleUid, mdTitle, mdGitPath, page);
//                md.setMdId(mdId);
//                md.setUploadUserName(userName);
//                md.setUploadUserSourceId(article.getUploadUserSource());
//            }
//        }
//    }

    public static void esSave(String articleUid, List<ConverterResult> mdList) {

        IEsService esService = (IEsService) ContextUtil.getBean("esService");

        Iterator<ConverterResult> it = mdList.iterator();
        while (it.hasNext()) {
            ConverterResult md = it.next();
            try {
                ESSetData esSetData = new ESSetData();
                esSetData.setArticle_uid(articleUid);
                esSetData.setTitle(CommonUtils.getFileNameNoExt(md.getName()));
                esSetData.setContent(CommonUtils.read(md.getAbsolutePath(), "UTF-8"));
                esSetData.setPath(md.getGitPath().replace("keepwork/baike", ""));
                esSetData.setPage(md.getPage());
                esSetData.setTotalpage(mdList.size());
                esSetData.setPublic_status(true);
                esSetData.setSource(md.getUploadUserSourceId());
                esSetData.setAuthor("");
                esSetData.setPublish_time("");

                String mdUid = esService.esIndex(esSetData);
                if (mdUid != null)
                    mysqlUpdateEsId(md.getMdId(), mdUid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mysqlUpdateEsIndexStatus(articleUid,true);
    }

    public static void esSaveAsyn(String articleUid, List<ConverterResult> mdList) {
        IKafkaService kafkaService = (IKafkaService) ContextUtil.getBean("kafkaService");

        try {
            kafkaService.kafkaProduceIndexMd(articleUid, mdList);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("upload imgs write kafka error:" + e.getMessage());
        }
    }

    public static void mysqlUpdateEsId(int mdId, String mdUid) {

        IMdService mdService = (IMdService) ContextUtil.getBean("mdService");

        mdService.indexStatusUpdate(mdId, mdUid);
    }

    public static void mysqlUpdateEsIndexStatus(String articleUid, boolean status) {

        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");

        articleService.indexStatusUpdate(articleUid, status);
    }

    public static boolean getArticleIndexStatus(String articleUid) throws NotFoundException {
        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");
        return articleService.getIndexStatus(articleUid);
    }

    /**
     * swf转换
     * @param md5
     * @return swf路径
     */
    public static String swfConvert(String md5) {
        IFileService fileService = (IFileService) ContextUtil.getBean("fileService");

        com.golaxy.converter.entity.mysql.File file = fileService.getFileByUid(md5);
        if (file == null)
            return null;

        boolean swfConvertRunning = file.getSwfConvertRunning();
        String swfPath = file.getSwfPath();
        if (swfPath==null || swfPath.equals("")) {
            String inputFilePath = GlobalVars.uploadRootPath + "/" + file.getPath();

            String inputPdfFilePath = inputFilePath.replace("."+CommonUtils.getFileExt(inputFilePath), ".pdf");
            if (new File(inputPdfFilePath).exists())
                inputFilePath = inputPdfFilePath;

            if (!swfConvertRunning) {
                fileService.swfPathUpdate(md5, true, null);
                try {
                    String outFilePath = Office2Swf.office2Swf(inputFilePath, null);
                    swfPath = CommonUtils.getRelativePath(GlobalVars.uploadRootPath, outFilePath);
                    fileService.swfPathUpdate(md5, false, swfPath);
                } catch (ConvertFailException e) {
                    e.printStackTrace();
                    fileService.swfPathUpdate(md5, false, null);
                }
            } else {
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    file = fileService.getFileByUid(md5);
                    swfConvertRunning = file.getSwfConvertRunning();
                    swfPath = file.getSwfPath();
                } while (swfConvertRunning);
            }
        }

        return swfPath;
    }

    public static int getConvertState(String md5) {
        IFileService fileService = (IFileService) ContextUtil.getBean("fileService");

        com.golaxy.converter.entity.mysql.File file = fileService.getFileByUid(md5);
        if (file == null)
            return -1;

        return file.getConvertStatus();
    }

    public static boolean getGitlabSaveStatus(String md5, String uid) {
        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");

        Article article = articleService.getArticleByUid(uid);
        if (article == null)
            return false;
        else
            return true;
    }

}
