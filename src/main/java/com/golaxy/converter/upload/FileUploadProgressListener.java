package com.golaxy.converter.upload;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.golaxy.converter.entity.frontend.ResponseResult;
import com.golaxy.converter.entity.frontend.StatusCode;
import com.golaxy.converter.utils.JackJsonUtils;
import com.golaxy.converter.websocket.SessionHandler;
import org.apache.commons.fileupload.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 文件上传进度监听
 */
public class FileUploadProgressListener implements ProgressListener {

	private final static Logger logger = LoggerFactory.getLogger(FileUploadProgressListener.class);

	private HttpSession session;
	// 上传唯一标示
	private String uid;
	// websocket session
	private WebSocketSession webSocketSession;
	// 进度: 0-100
	private int progress = 0;
	// 实时进度
	private float realProgress = 0;
	
	private long lastTime = System.currentTimeMillis();
	private long lastRead = 0L;

	public FileUploadProgressListener() {  }  
	
    public FileUploadProgressListener(HttpSession session, HttpServletRequest request) {
        this.session=session;  
        Progress status = new Progress();
        session.setAttribute("upload_ps", status);
        
        uid = request.getParameter("uid");
        webSocketSession = SessionHandler.getSession(uid);
        
        String userName = request.getParameter("username");
		if (userName==null || userName.equals("")) {
			String result = "{\"code\":\"202\",\"msg\":\"no username\"}";
			logger.info("[文件上传]: 用户名:" + userName + " | uid: " + uid + "|" + result);
			SessionHandler.closeSession(webSocketSession);
			return;			
		} else {
			logger.info("[文件上传]: 用户名：" + userName + " | uid: " + uid);
		}   
    }  

    /**
     * 更新进度、速度
     * @param pBytesRead      到目前为止读取文件的比特数
     * @param pContentLength  文件总大小
     * @param pItems          目前正在读取第几个文件
     */
	public void update(long pBytesRead, long pContentLength, int pItems) {
		Progress status = (Progress) session.getAttribute("upload_ps");
		status.setBytesRead(pBytesRead);
		status.setContentLength(pContentLength);
		status.setItems(pItems);
		session.setAttribute("upload_ps", status);
		ResponseResult result;
		
		// 上传进度, 0-100
		realProgress = (float)pBytesRead/pContentLength * 100;
		if (progress != (int) realProgress) {
			progress = (int) realProgress;
			result = new ResponseResult();
			result.setCode(StatusCode.WEBSOCKET_UPLOAD_PROGRESS);
			result.setUid(uid);
			result.setProgress(progress);

            SessionHandler.sendMessage(webSocketSession, JackJsonUtils.toJson(result));

		}
		
		long currTime = System.currentTimeMillis();
		// 每秒实时速度
		if (pBytesRead < pContentLength) {
			
			long diffTime = currTime - lastTime;
			if ( diffTime>=1000 ) {
				lastTime = currTime;
				long bytesRead = pBytesRead - lastRead; 
				lastRead = pBytesRead;
                result = new ResponseResult();
                result.setCode(StatusCode.WEBSOCKET_UPLOAD_SPEED);
                result.setUid(uid);
                result.setSpeed(getSpeed(bytesRead));

                SessionHandler.sendMessage(webSocketSession, JackJsonUtils.toJson(result));

			}
		}
		
		// 平均速度
		if (pBytesRead == pContentLength) {

			long timeSpend = currTime-status.getStartReatTime();
			timeSpend = (timeSpend==0) ? 1 : timeSpend; // 小文件上传时间太短有可能timeSpend为0

            result = new ResponseResult();
            result.setCode(StatusCode.WEBSOCKET_UPLOAD_SPEED);
            result.setUid(uid);
            result.setSpeed(getSpeed(pBytesRead*1000 / timeSpend));

            SessionHandler.sendMessage(webSocketSession, JackJsonUtils.toJson(result));
		}
	}

    /**
     * 计算速度
     * @param bytesRead 每秒读的字节数
     * @return
     */
	private String getSpeed(long bytesRead) {
		String speedStr = "0KB/s";
		float speed = (float)bytesRead/1000;		
		if (speed<1000)
			speedStr = (int)speed + "KB/s";
		else 
			speedStr = String.format("%.1f",speed/1000) + "MB/s";
		return speedStr;
	}
}
