package com.golaxy.converter.convert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.golaxy.converter.entity.kafka.TopicInfo;
import com.golaxy.converter.utils.CommonUtils;
import org.springframework.web.socket.WebSocketSession;


/**
 * Created by yangzongze on 2017/12/1.
 *
 * 全局变量
 */
public class GlobalVars {
	
	public static Properties properties = new Properties();
	private static InputStream in = GlobalVars.class.getClassLoader().getResourceAsStream("config.properties");

	/** 文档类型 **/
	public static final String office = "office";
	public static final String txt    = "txt";
	public static final String ocr    = "ocr";
	
	/** kafka topic名字 **/
	public static final String topicOffice;
	public static final String topicTxt;
	public static final String topicOcr;
    public static final String topicImgUpload;
    public static final String topicMdIndex;

	/** doc/docx/ppt/pptx队列 **/
	public static long currOfficeProcessOffset;  //当前正在转换的文件的offset
	public static Map<String, WebSocketSession> officeSessions = new ConcurrentHashMap<>();
	
	/** pdf/html/htm/txt队列 **/
	public static long currTxtProcessOffset;  //当前正在转换的文件的offset
	public static Map<String, WebSocketSession> txtSessions = new ConcurrentHashMap<>();
	
	/** pdf OCR队列 **/
	public static long currOcrProcessOffset;  //当前OCR线程正在转换的文件的offset
	public static Map<String, WebSocketSession> ocrSessions = new ConcurrentHashMap<>();
	
	public static Map<String, TopicInfo> topics = new ConcurrentHashMap<>();

	public static String uploadPath;
    public static String uploadRootPath;
    public static String encoding;
    public static String mdServer;
	public static String kafkaServer;
    public static String DConverterPath;
    public static String gitlabRawBaseUrl;
    public static String keepworkUserProj;
    /** 是否OCR转换 **/
    public static boolean OCR;

	static {
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		topicOffice = properties.getProperty("topicOffice");
		topicTxt = properties.getProperty("topicTxt");
		topicOcr = properties.getProperty("topicOcr");
		topicImgUpload = properties.getProperty("topicImgUpload");
		topicMdIndex = properties.getProperty("topicMdIndex");

		uploadPath = properties.getProperty("uploadPath");
        uploadRootPath = properties.getProperty("uploadRootPath");
		encoding = System.getProperty("os.name").toLowerCase().startsWith("win") ? "GBK" : "UTF-8";
        mdServer = properties.getProperty("mdServer");
        kafkaServer = properties.getProperty("kafkaServer");
        DConverterPath = properties.getProperty("DConverter_path");
        gitlabRawBaseUrl = properties.getProperty("gitlabRawBaseUrl");
        keepworkUserProj = properties.getProperty("keepworkUserProj");

        switch(properties.getProperty("OCR").toLowerCase()) {
            case "yes":
                OCR = true;
                break;
            case "no":
                OCR = false;
                break;
            default:
                OCR = false;
        }

		topics.put(topicOffice, new TopicInfo(topicOffice, "Convert_Office", Integer.parseInt( GlobalVars.properties.getProperty("threadOfficeNum") )));
		topics.put(topicTxt, new TopicInfo(topicTxt, "Convert_Txt", Integer.parseInt( GlobalVars.properties.getProperty("threadTxtNum") )));
		topics.put(topicOcr, new TopicInfo(topicOcr, "Convert_Ocr", Integer.parseInt( GlobalVars.properties.getProperty("threadOcrNum") )));
	}
	
	/**
	 * topic对应的session组中存入新的session
	 * @param topic
	 * @param key
	 * @param session
	 */
	public static void put(String topic, String key, WebSocketSession session) {
		if (topic.equals(topicOffice))
			officeSessions.put(key, session);
        if (topic.equals(topicTxt))
			txtSessions.put(key, session);
        if (topic.equals(topicOcr))
			ocrSessions.put(key, session);
	}
	
	public static void removeSession(WebSocketSession session) {
		
		Iterator<Map.Entry<String, WebSocketSession>> it = ocrSessions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, WebSocketSession> entry=it.next();  
            if(session.equals(entry.getValue())){  
                it.remove();
                break;
            }  
        }  
		
		it = txtSessions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, WebSocketSession> entry=it.next();  
            if(session.equals(entry.getValue())){  
                it.remove();
                break;
            }  
        } 
		
		it = officeSessions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, WebSocketSession> entry=it.next();  
            if(session.equals(entry.getValue())){  
                it.remove();
                break;
            }  
        } 
	}
	
	/**
	 * 根据topic返回其对应的session组
	 * @param topic
	 * @return
	 */
	public static Map<String, WebSocketSession> getSessionGroupByTopic(String topic) {		

        if (topic.equals(topicOffice))
			return officeSessions;
        if (topic.equals(topicTxt))
			return txtSessions;
        if (topic.equals(topicOcr))
			return ocrSessions;

        return null;
	}
	
	/**
	 * 根据key(uid)返回其对应的session组
	 * @param key
	 * @return
	 */
	public static Map<String, WebSocketSession> getSessionGroupByUid(String key) {		
		if (officeSessions.containsKey(key))
			return officeSessions; 
		else if (txtSessions.containsKey(key))
			return txtSessions;
		else if (ocrSessions.containsKey(key))
			return ocrSessions;

		return null;
	}
	
	/**
	 * 设置某个topic队列现在正在处理的文档的offset
	 * @param topic
	 * @param offset
	 */
	public static void setProcessingOffset(String topic, long offset) {

        if (topic.equals(topicOffice))
			currOfficeProcessOffset = offset;
        if (topic.equals(topicTxt))
			currTxtProcessOffset = offset;
        if (topic.equals(topicOcr))
			currOcrProcessOffset = offset;
	}
	
	/**
	 * 判断文件类型按那种转换
	 * @param fileName
	 */
	public static String judgeFileType(String fileName) {
		String result = null;
		String fileExt = fileName.substring(fileName.lastIndexOf(".")+1);
		switch (fileExt) {
			case "doc":
			case "docx":
			case "ppt":
			case "pptx":
				result = GlobalVars.office;
				break;
			case "html":
			case "htm":
			case "txt":
				result = GlobalVars.txt;
				break;
			case "pdf":
				if (GlobalVars.OCR && CommonUtils.isPhotocopyPdf(fileName))
					result = GlobalVars.ocr;
				else
					result = GlobalVars.txt;
				break;
			default:
				result = GlobalVars.txt;
				break;
		}
		return result;
	}
	
	/**
	 * 根据文档类型返回相应的kafka队列topic
	 * @param fileType
	 * @return
	 */
	public static String getKafkaTopic(String fileType) {
		String topic = null;
		switch (fileType) {
			case office:
				topic = topicOffice;
				break;
			case txt:
				topic = topicTxt;
				break;
			case ocr:
				topic = topicOcr;
				break;
			default:
				topic = topicTxt;
				break;
		}
		return topic;
	}
	
	/**
	 * 根据topic名字返回相应队列中正在处理的offset
	 * @param topic
	 * @return
	 */
	public static long getCurrProcessOffset(String topic) {
		long currProcessOffset = 0L;

        if (topic.equals(topicOffice))
			currProcessOffset = currOfficeProcessOffset;
        if (topic.equals(topicTxt))
			currProcessOffset = currTxtProcessOffset;
        if (topic.equals(topicOcr))
			currProcessOffset = currOcrProcessOffset;

		return currProcessOffset;
	}
}

