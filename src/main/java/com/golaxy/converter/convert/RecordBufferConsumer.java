package com.golaxy.converter.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.golaxy.converter.entity.frontend.ResponseResult;
import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.entity.frontend.StatusCode;
import com.golaxy.converter.entity.kafka.KafkaUserFile;
import com.golaxy.converter.exception.ConvertFailException;
import com.golaxy.converter.utils.CommonUtils;
import com.golaxy.converter.utils.JackJsonUtils;
import com.golaxy.converter.websocket.SessionHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;


/**
 * Created by yangzongze on 2017/12/1.
 *
 * 转换处理线程
 */
public class RecordBufferConsumer implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(RecordBufferConsumer.class);
	private RecordBuffer recordBuffer;
	
	public RecordBufferConsumer(RecordBuffer recordBuffer) {
		this.recordBuffer = recordBuffer;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				@SuppressWarnings("unchecked")
				ConsumerRecord<String, String> record = (ConsumerRecord<String, String>) recordBuffer.take();
				runTask(record);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private void runTask(ConsumerRecord<String, String> record) {
    	
    	//待转换用户session
    	WebSocketSession sessionCurr = null;
    	String uidCurr = record.key();
    	KafkaUserFile userFile = JackJsonUtils.fromJson(record.value(), KafkaUserFile.class);
    	String fileMd5 = userFile.getFileMd5();
    	String userName = userFile.getUserName();
    	String userSource = userFile.getUserSource();
    	String fileName = userFile.getFileName();
    	String articleUid = uidCurr;
    	Integer cateId = userFile.getCateId();
    	String articleName = CommonUtils.getFileNameNoExt(fileName);
    	long offsetCurr = record.offset();
        ResponseResult res;

    	logger.info(Thread.currentThread().getName()+"[Kafka]: 读取成功 offset:"+offsetCurr+" | md5 :"+fileMd5+" | uid: " +uidCurr+ " | 文件名:" + fileName);


    	// 获取转换状态
        int convertState = MdSave.getConvertState(fileMd5);
        switch (convertState) {
            case StatusCode.MYSQL_CONVERT_FINISHED_SUCCESS:
                boolean gitlabSaveStatus = MdSave.getGitlabSaveStatus(fileMd5, uidCurr);
                if (gitlabSaveStatus)
                    return;
                else {

                }
                break;
            default:
                if (!MdSave.mysqlSaveRemoteArticle(articleUid, fileMd5, articleName, userName, userSource, cateId)) {
                    return;
                }
                break;
        }

    	// 设置当前所在组正在转换的offset为所在队列正在转换的offset
    	GlobalVars.setProcessingOffset(record.topic(), offsetCurr);
    	// 有可能这里读出来了但是还没有写入session
    	// 为了确保能读到session组中已经有了该session，这里不停的读直到读等到读到为止，
    	// 根据uidCurr判断属于哪一组session
    	Map<String, WebSocketSession> sessions = GlobalVars.getSessionGroupByUid(uidCurr);
    	// kafka读取有可能比kafka写入回调函数执行快导致拿不到session，顾多拿几次
    	if (sessions==null) {
    		for (int i=0; sessions==null&&i<100; i++) {
	    		try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    		sessions = GlobalVars.getSessionGroupByUid(uidCurr);
    		}
    	}
    	if (sessions!=null) {
            for (Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                String uid = entry.getKey();
                WebSocketSession session = entry.getValue();
                long offset = Long.parseLong((String) session.getAttributes().get("offset"));
                long waitQueueNum = 0;

                if (uid.equals(uidCurr)) {
                    sessionCurr = session;
                    waitQueueNum = 0;
                } else {
                    waitQueueNum = offset - offsetCurr;
                }
                if (waitQueueNum >= 0) {
                    res = new ResponseResult();
                    res.setCode(StatusCode.WEBSOCKET_CONVERT_QUEUE);
                    res.setUid(uid);
                    res.setWaitqueue((int) waitQueueNum);

                    SessionHandler.sendMessage(session, JackJsonUtils.toJson(res));
                }
            }
        }
    		
    		// 用来保存转换结果
        List<ConverterResult> mdList = new ArrayList<>();
        List<ConverterResult> imgList = new ArrayList<>();

        logger.info(Thread.currentThread().getName()+"[文档转换]: 转换开始 | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);

        //发送开始转换信号
        res = new ResponseResult();
        res.setCode(StatusCode.WEBSOCKET_CONVERT_RUNNING);
        res.setUid(uidCurr);
        res.setMsg("convert start");
        SessionHandler.sendMessage(uidCurr, JackJsonUtils.toJson(res));

        MdSave.mysqlUpdateConvertStatus(fileMd5, StatusCode.MYSQL_CONVERT_RUNNING, null);
        logger.info(Thread.currentThread().getName()+"[文档转换]: 转换中.. | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);

        boolean convertSuccess = false;
        try {
            // md转换
            convertSuccess = Converter.converter(fileName, mdList, imgList);

            MdSave.mysqlUpdateConvertStatus(fileMd5, StatusCode.MYSQL_CONVERT_FINISHED_SUCCESS, null);
            logger.info(Thread.currentThread().getName()+"[文档转换]: 转换成功 | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);

            // 发送转换结果
            res = new ResponseResult();
            res.setCode(StatusCode.WEBSOCKET_CONVERT_SUCCESS);
            res.setUid(uidCurr);
            res.setMsg("convert success");
            res.setArticle_name(articleName);
            res.setImgList(imgList);
            res.setMdList(mdList);
        } catch (ConvertFailException e) {
            e.printStackTrace();

            MdSave.mysqlUpdateConvertStatus(fileMd5, StatusCode.MYSQL_CONVERT_FINISHED_FAILURE, e.getMessage());
            logger.error(Thread.currentThread().getName()+"[文档转换]: 转换失败 | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);

            res = new ResponseResult();
            res.setCode(StatusCode.WEBSOCKET_CONVERT_FAILURE);
            res.setUid(uidCurr);
            res.setMsg("convert failure");
            SessionHandler.sendMessage(uidCurr, JackJsonUtils.toJson(res));
            SessionHandler.closeSession(uidCurr);
        } finally {
            boolean noticeStatus = SessionHandler.sendMessage(sessionCurr, JackJsonUtils.toJson(res));
            MdSave.mysqlUpdateNotice(fileMd5, noticeStatus);
        }

        if (convertSuccess) {
            boolean saveStatue;
            res = new ResponseResult();
            res.setUid(uidCurr);
            // 转换结果本地路径保存
            MdSave.mysqlSaveLocal(fileMd5, imgList, mdList);
            logger.info(Thread.currentThread().getName()+"[mysql]: 本地路径保存 | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);
            // 保存gitlab
            logger.info(Thread.currentThread().getName()+"[gitlab]: gitlab上传中.. | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);
            saveStatue = MdSave.gitlabSave(articleName, userName, fileMd5, mdList, imgList);
            if (saveStatue) {
                logger.info(Thread.currentThread().getName()+"[gitlab]: gitlab上传成功 | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);

                // 保存gitlab存储路径
                MdSave.mysqlSaveRemoteMd(articleUid, userName, mdList);
                logger.info(Thread.currentThread().getName()+"[mysql]: gitlab路径保存 | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);

                // 提交ES建索引并更新索引ID到MySQL
                MdSave.esSaveAsyn(articleUid, mdList);

                res.setCode(StatusCode.WEBSOCKET_GITLAB_SAVE_SUCCESS);
                res.setMsg("save success");
                res.setArticle_name(articleName);
                res.setMdList(mdList);
                switch (CommonUtils.getFileExt(fileName)) {
                    case "doc":
                    case "docx":
                    case "ppt":
                    case "pptx":
                    case "pdf":
                        // 转swf
                        MdSave.swfConvert(fileMd5);
                        res.setPreview_url(GlobalVars.mdServer + "/ConverterServer/file/preview?md5=" + fileMd5);
                        break;
                    default:
                        break;
                }
            } else {
                logger.error(Thread.currentThread().getName()+"[gitlab]: gitlab上传失败 | md5 :"+fileMd5+" | uid: "+uidCurr+" | 文件名: "+fileName);
                res.setCode(StatusCode.WEBSOCKET_GITLAB_SAVE_FAILURE);
                res.setMsg("save failure");
                MdSave.mysqlDeleteArticle(articleUid);
            }
            SessionHandler.sendMessage(uidCurr, JackJsonUtils.toJson(res));
            SessionHandler.closeSession(uidCurr);
        }
	}
	
}

