package com.golaxy.converter.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.golaxy.converter.business.UploadBusiness;
import com.golaxy.converter.convert.MdSave;
import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.entity.frontend.ResponseResult;
import com.golaxy.converter.entity.frontend.StatusCode;
import com.golaxy.converter.utils.CommonUtils;
import com.golaxy.converter.utils.JackJsonUtils;
import com.golaxy.converter.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 文档上传相关接口
 */
@Controller
@RequestMapping("/file")
public class UploadController {
	
	private final static Logger logger = LoggerFactory.getLogger(UploadController.class);
	
	/**
	 * 上传前检查是否已存在改文件
	 * @param request
	 * @param response
	 */
	@RequestMapping("/check")
	public void check(HttpServletRequest request, HttpServletResponse response) {

        String userName = request.getParameter("username");
		String md5 = request.getParameter("md5");
        ResponseResult result = new ResponseResult();
		
		if (userName==null || userName.equals("") || md5==null || md5.equals("")) {
            result.setCode(StatusCode.UPLOAD_MD5_REQUIRED);
            result.setMsg("param md5 required");
		} else {
			md5 = md5.toLowerCase();
			String pattern = "^([a-z0-9]{32})$"; 
		    boolean isMatch = md5.matches(pattern);
		    if (!isMatch) {
                result.setCode(StatusCode.UPLOAD_MD5_INVALID);
                result.setMsg("param md5 invalid");
		    } else {
		    	if ( UploadBusiness.checkExist(md5) ) {
		    	    if ( UploadBusiness.checkUserUploaded(md5, userName) ) {
                        result.setCode(StatusCode.USER_FILE_EXSIT);
                        result.setMsg("you have uploaded this file");
                    } else {
                        result.setCode(StatusCode.RAW_FILE_EXSIT);
                        result.setMsg("original file has exist");
                    }
                } else {
                    result.setCode(StatusCode.RAW_FILE_NONEXSIT);
                    result.setMsg("original file not exist");
                }
		    }		    
		}		

		ResponseUtils.renderJson(response, JackJsonUtils.toJson(result));
	}

    /**
     * 获取转换结果
     * @param request
     * @param response
     */
    @RequestMapping("/result")
    public void getResult(HttpServletRequest request, HttpServletResponse response) {

        String userName = request.getParameter("username");
        String articleName = request.getParameter("filename");
        String md5 = request.getParameter("md5");
        String userSource = request.getParameter("usersource");
        String cateIdStr = request.getParameter("cate_id");
        ResponseResult result = new ResponseResult();

        if (userName==null || userName.equals("") || articleName==null || articleName.equals("") || md5==null || md5.equals("")) {
            result.setCode(StatusCode.UPLOAD_PARAMS_LACK);
            result.setMsg("username/filename/md5 must be required");
        } else {
            md5 = md5.toLowerCase();
            String pattern = "^([a-z0-9]{32})$";
            boolean isMatch = md5.matches(pattern);
            if (!isMatch) {
                result.setCode(StatusCode.UPLOAD_MD5_INVALID);
                result.setMsg("param md5 invalid");
            } else {
                List<ConverterResult> results = UploadBusiness.getResult(userName, md5);
                List<ConverterResult> imgList = new ArrayList<>();
                List<ConverterResult> mdList = new ArrayList<>();
                for (ConverterResult res : results) {
                    if (res.getPage() == null)
                        imgList.add(res);
                    else
                        mdList.add(res);
                }
                result.setCode(StatusCode.SUCESS_CODE);
                result.setMsg("success");
                result.setImgList(imgList);
                List<ConverterResult> mdList1 = new ArrayList<>();
                Iterator<ConverterResult> it = mdList.iterator();
                while (it.hasNext()) {
                    mdList1.add(it.next().clone());
                }
                result.setMdList(mdList1);

                articleName = CommonUtils.getFileNameNoExt(articleName.trim());
                userSource = userSource==null ? "" : userSource;
                Integer cateId = 0; //代表未分类
                if (cateIdStr!=null && !cateIdStr.equals("")) {
                   try {
                       cateId = Integer.parseInt(cateIdStr);
                   } catch (Exception e) {
                       cateId = 0;
                   }
                }
                String articleUid = CommonUtils.getUniqueId();
                MdSave.gitlabSave(articleName, userName, mdList, imgList);
                MdSave.mysqlSaveRemote(articleUid, md5, articleName, userName, userSource, cateId, mdList);
                MdSave.esSave(articleUid, mdList);
            }
        }

        ResponseUtils.renderJson(response, JackJsonUtils.toJson(result));
    }
	
	/**
	 * 上传
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/upload")
	public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String userName = request.getParameter("username");
        String md5 = request.getParameter("md5");
		String uid = request.getParameter("uid");
		String userSource = request.getParameter("resource");
		String cateIdStr = request.getParameter("cate_id");

        ResponseResult result = new ResponseResult();
		
		if (request.getCharacterEncoding() == null) {
			request.setCharacterEncoding("UTF-8");
		}
		if (userName == null || userName.equals("") || uid == null || uid.equals("") || md5 == null || md5.equals("")) {
            result.setCode(StatusCode.UPLOAD_PARAMS_LACK);
            result.setMsg("username/uid/md5 must be required");
		} else {
            md5 = md5.toLowerCase();
            String pattern = "^([a-z0-9]{32})$";
            boolean isMatch = md5.matches(pattern);
            if (!isMatch) {
                result.setCode(StatusCode.UPLOAD_MD5_INVALID);
                result.setMsg("param md5 invalid");
            } else {
                Integer cateId = 0; //代表未分类
                if (cateIdStr!=null && !cateIdStr.equals("")) {
                    try {
                        cateId = Integer.parseInt(cateIdStr);
                    } catch (Exception e) {
                        cateId = 0;
                    }
                }

                userSource = userSource==null ? "" : userSource;

                if ( UploadBusiness.upload((MultipartHttpServletRequest) request, md5, uid, userName, userSource, cateId) ) {
                    result.setCode(StatusCode.UPLOAD_SUCCESS);
                    result.setMsg("upload success");
                } else {
                    result.setCode(StatusCode.UPLOAD_FAILURE);
                    result.setMsg("upload failure");
                }
            }
		}
		logger.info("[文件上传Mod]: <<<<<<<<<< 用户:"+ userName + " | " + result);

		ResponseUtils.renderJson(response, JackJsonUtils.toJson(result));
	}

    /**
     * 消息提醒,获取用户没有读取过的转换成功/失败消息
     * @param request
     * @param response
     */
    @RequestMapping("/notice")
    public void getNotice(HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * 预览
     * @param request
     */
    @RequestMapping("/preview")
    public ModelAndView preview(HttpServletRequest request) {

        String articleId = request.getParameter("article_id");
        String swfPath = null;

        if (articleId == null || articleId.equals("")) {
            return new ModelAndView("error", "msg", "param article_id must be required");
        } else {
            swfPath = UploadBusiness.preview(Integer.valueOf(articleId));
            if (swfPath == null)
                return new ModelAndView("error", "msg", "no such file");
        }

        return new ModelAndView("preview", "swfPath", "/"+swfPath);
    }
}
