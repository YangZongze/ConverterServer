package com.golaxy.converter.websocket;

import com.golaxy.converter.convert.GlobalVars;
import com.golaxy.converter.entity.frontend.ResponseResult;
import com.golaxy.converter.entity.frontend.StatusCode;
import com.golaxy.converter.utils.CommonUtils;
import com.golaxy.converter.utils.JackJsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * websocket操作
 */
public class WebsocketHandler extends AbstractWebSocketHandler {
	
	private final static Logger logger = LoggerFactory.getLogger(WebsocketHandler.class);

    public static int onlineCount = 0;  //当前在线人数

    public static List<WebSocketSession> sessions = new ArrayList<WebSocketSession>();

    /**
     * 建立连接后执行
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        
        String uid = CommonUtils.getUniqueId();
        
        logger.info("[websocket]: 连接: " + uid);      
        session.getAttributes().put("uid", uid);
        ResponseResult result = new ResponseResult();
        result.setCode(StatusCode.WEBSOCKET_CONNECTED);
        result.setUid(uid);
        session.sendMessage(new TextMessage(JackJsonUtils.toJson(result)));
        sessions.add(session);
        WebsocketHandler.addOnlineCount(); 

        super.afterConnectionEstablished(session);
    }

    /**
     * 关闭连接后执行
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    	
        sessions.remove(session);
        GlobalVars.removeSession(session);
        logger.info("[websocket]: 断开: " + session.getAttributes().get("uid"));
        WebsocketHandler.reduceOnlineCount();
    }
    
    /**
     * 处理收到的消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	
        logger.info("收到"+ session.getAttributes().get("uid") +"消息:" + message.getPayload());     
    }
    
    /**
     * 发送消息
     * @param session
     * @param message
     * @throws Exception
     */
    public void sendMessage(WebSocketSession session, String message) throws Exception  {
    	
    	if (session != null && session.isOpen()) {
    		synchronized (session) {
    			session.sendMessage(new TextMessage(message));
			}	
    	} 	
    }
    
    /**
     * 广播消息
     * @param message
     * @throws Exception
     */
    public void broadcast(String message) throws Exception {

        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(message));
        }
    }
    
    public static void addOnlineCount() {
        onlineCount++;
    }

    public static void reduceOnlineCount() { 
    	onlineCount--; 
    }
    
}  