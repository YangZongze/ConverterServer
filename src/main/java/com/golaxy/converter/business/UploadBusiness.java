package com.golaxy.converter.business;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.golaxy.converter.convert.GlobalVars;
import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.entity.frontend.StatusCode;
import com.golaxy.converter.entity.mysql.Article;
import com.golaxy.converter.entity.mysql.MdLocal;
import com.golaxy.converter.service.mysql.IArticleService;
import com.golaxy.converter.service.mysql.IFileService;
import com.golaxy.converter.service.mysql.IMdLocalService;
import com.golaxy.converter.utils.CommonUtils;
import com.golaxy.converter.utils.ContextUtil;
import com.golaxy.converter.service.kafka.IKafkaService;
import com.golaxy.converter.utils.Office2Swf;
import com.golaxy.converter.websocket.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 文档上传业务处理
 */
public class UploadBusiness {

	private final static Logger logger = LoggerFactory.getLogger(UploadBusiness.class);
	private static String uploadPath = GlobalVars.uploadPath;
	private static String encoding = GlobalVars.encoding;

    /**
     * 上传前检查文档库中是否已存在
     *
     * @param md5 文档md5
     * @return
     */
    public static boolean checkExist(String md5) {

        IFileService fileService = (IFileService) ContextUtil.getBean("fileService");

        return fileService.fileSearch(md5)>0 ? true : false;
    }

    /**
     * 上传前检查用户是否已上传过
     *
     * @param userName 用户名
     * @param md5 文档md5
     * @return
     */
    public static boolean checkUserUploaded(String md5, String userName) {

        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");

        return articleService.articleSearch(md5, userName)>0 ? true : false;
    }

    /**
     * 获取转换结果
     *
     * @param md5
     */
    public static List<ConverterResult> getResult(String userName, String md5) {

        IMdLocalService mdLocalService = (IMdLocalService) ContextUtil.getBean("mdLocalService");

        List<ConverterResult> resultList = new ArrayList<>();

        for(MdLocal mdLocal : mdLocalService.getResultByMd5(md5)) {
            ConverterResult result = new ConverterResult();

            result.setName(mdLocal.getName());
            result.setUrl(GlobalVars.mdServer + "/" + mdLocal.getPath());
            result.setName(mdLocal.getName());
            result.setAbsolutePath(GlobalVars.uploadPath + "/" + mdLocal.getPath());
            if (mdLocal.getType() == 1) {
                result.setPage(mdLocal.getPage());
            }

            resultList.add(result);
        }

        return resultList;
    }

	/**
	 * 文件保存
	 *
	 * @param multipartRequest
	 * @param uid
	 * @param userName
	 * @return
	 */
	public static boolean upload(MultipartHttpServletRequest multipartRequest, String md5, String uid,
                                String userName, String userSource, int cateId) throws IOException {
		WebSocketSession session = SessionHandler.getSession(uid);

		boolean status = false;

		String uploadFileName = save(multipartRequest, md5);
		if (uploadFileName != null) {
		    // 1.保存mysql
            status = mysqlSave(md5, uploadFileName);
            // 2.排队等待
            waitInQueue(uid, md5, userName, uploadFileName, userSource, cateId);
		} else {
			SessionHandler.closeSession(session);
		}
		logger.info("[文档上传Mod]: 用户名:" + userName + " | 文件名:" + uploadFileName);

		return status;
	}

	/**
	 * 单个文件保存
	 *
	 * @param multipartRequest
     * @param md5
	 * @return 保存在磁盘上绝对路径名称
	 */
	private static String save(MultipartHttpServletRequest multipartRequest, String md5) throws IOException {

		String uploadFileName = null;
		String savePath = uploadPath + "/upload/" + CommonUtils.getStringDate();
        boolean status = false;

        File saveDir = new File(savePath);
        if (!saveDir.exists())
            saveDir.mkdirs();

		Iterator<?> iter = multipartRequest.getFileNames();
		while (iter.hasNext()) {
			MultipartFile file = multipartRequest.getFile(iter.next().toString());
			if (file != null) {
				String fileName = file.getOriginalFilename();
				try {
					fileName = new String(fileName.getBytes(), encoding);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					//throw new Exception();
                    fileName = md5;
				}
				fileName = fileName.replaceAll("[ 　]{1,}", "_");
                int dotIndex = fileName.lastIndexOf(".");
                String fileNameNoExt = fileName.substring(0, dotIndex);
                String uploadFilePath = savePath + "/" + fileNameNoExt;
                String path = uploadFilePath;
                int i = 1;
                do {
                    File uploadPath = new File(path);
                    if (uploadPath.isDirectory() && uploadPath.exists()) {
                        String iStr = "(" + (i++) + ")";
                        path = uploadFilePath + iStr;
                    } else {
                        uploadPath.mkdirs();
                        uploadFileName = uploadPath + "/" + fileName;
                        file.transferTo(new File(uploadFileName));
                        status = true;
                    }
                } while (!status);
			}
			break;
		}

		return status ? uploadFileName.replaceAll("[//\\\\]", "/") : null;
	}

    /**
     * 保存原始文件路径
     * @param md5
     * @param fileName
     * @return
     */
    private static boolean mysqlSave(String md5, String fileName) {

        IFileService fileService = (IFileService) ContextUtil.getBean("fileService");

        String fileNameNoExt = CommonUtils.getFileNameNoExt(fileName);
        String fileType = CommonUtils.getFileExt(fileName);
        String path = CommonUtils.getRelativePath(uploadPath, fileName);

        int id = fileService.fileAdd(md5, fileNameNoExt, path, fileType);

        return id>0 ? true: false;
    }

    /**
     * 排队等待
     * @param uid
     * @param md5
     * @param userName
     * @param uploadFileName
     * @param userSource
     * @param cateId
     * @return 排队是否成功
     */
    private static boolean waitInQueue(String uid, String md5, String userName,
                                     String uploadFileName, String userSource, int cateId) {

        boolean status = false;
        IKafkaService kafkaService = (IKafkaService) ContextUtil.getBean("kafkaService");
        IFileService fileService = (IFileService) ContextUtil.getBean("fileService");

        try {
            status = kafkaService.kafkaProduce(uid, md5, userName, uploadFileName, userSource, cateId);
            if (status)
                fileService.convertStatusUpdate(md5, StatusCode.MYSQL_CONVERT_INQUEUE_SUCCESS, null);
            else
                fileService.convertStatusUpdate(md5, StatusCode.MYSQL_CONVERT_INQUEUE_FAILURE, null);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("write kafka error:" + e.getMessage());
            fileService.convertStatusUpdate(md5, StatusCode.MYSQL_CONVERT_INQUEUE_FAILURE, "write kafka error");
        }

        return status;
    }

    /**
     * 获取要预览的文件路径
     * @param articleId
     */
    public static String preview(int articleId) {

        IFileService fileService = (IFileService) ContextUtil.getBean("fileService");
        IArticleService articleService = (IArticleService) ContextUtil.getBean("articleService");

        Article article = articleService.getArticleById(articleId);
        if (article == null)
            return null;

        String md5 = article.getFileMd5();
        com.golaxy.converter.entity.mysql.File file = fileService.getFileByUid(md5);
        if (file == null)
            return null;

        String swfPath = file.getSwfPath();
        if (swfPath==null || swfPath.equals("")) {
            String inputFilePath = GlobalVars.uploadPath + "/" + file.getPath();

            String inputPdfFilePath = inputFilePath.replace("."+CommonUtils.getFileExt(inputFilePath), ".pdf");
            if (new File(inputPdfFilePath).exists())
                inputFilePath = inputPdfFilePath;

            String outFilePath = Office2Swf.office2Swf(inputFilePath, null);

            swfPath = CommonUtils.getRelativePath(GlobalVars.uploadPath, outFilePath);

            fileService.swfPathUpdate(md5, swfPath);
        }

        return swfPath;
    }

}