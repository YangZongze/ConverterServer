package com.golaxy.converter.upload;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 文件上传Resolver
 */
public class CustomMultipartResolver extends CommonsMultipartResolver {
	
	private HttpServletRequest request;
	
    protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {  
        ServletFileUpload upload = new ServletFileUpload(fileItemFactory);  
        upload.setSizeMax(-1);  
        if (request != null) {  
        	HttpSession session = request.getSession();
        	FileUploadProgressListener progressListener = new FileUploadProgressListener(session, request);
            upload.setProgressListener(progressListener); 
        }  
        return upload;  
    }  
    
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request, HttpServletResponse response) throws MultipartException {  
    	// 获取到request,要用到session  
        this.request = request;
        return super.resolveMultipart(request);
    }  
	
	@Override
	public MultipartParsingResult parseRequest(HttpServletRequest request) throws MultipartException {
		HttpSession session = request.getSession(); 
		String encoding = determineEncoding(request);
		FileUpload fileUpload = prepareFileUpload(encoding);
		FileUploadProgressListener progressListener = new FileUploadProgressListener(session, request);
		fileUpload.setProgressListener(progressListener);  
		try {
			List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
			return parseFileItems(fileItems, encoding);
		} catch (FileUploadBase.SizeLimitExceededException ex) {
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
		} catch (FileUploadException ex) {
			throw new MultipartException("Could not parse multipart servlet request", ex);
		}
	}
}
