package com.golaxy.converter.websocket;

import java.io.IOException;
import java.util.Iterator;

import com.golaxy.converter.convert.GlobalVars;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static com.golaxy.converter.websocket.WebsocketHandler.sessions;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * websocket session操作
 */
public class SessionHandler {

	/**
	 * 给session添加属性
	 * @param session
	 * @param key
	 */
	public static void addAttr(WebSocketSession session, String key, String value) {
		session.getAttributes().put(key, value);
	}
	
	/**
	 * 根据uid获取session
	 * @param uid
	 */
	public static WebSocketSession getSession(String uid) {
		WebSocketSession session = null;
		Iterator<WebSocketSession> it = sessions.iterator();
        while (it.hasNext()) {
        	session = it.next();
        	if (session.getAttributes().get("uid").equals(uid)) {
        		break;
        	}
        }
        return session;
	}
	
	/**
	 * 根据uid给session添加属性
	 * @return
	 */
	public static WebSocketSession addAttr(String uid, String key, String value) {
		WebSocketSession session = null;
		Iterator<WebSocketSession> it = sessions.iterator();
        while (it.hasNext()) {
        	session = it.next();
        	if (session.getAttributes().get("uid").equals(uid)) {
        		addAttr(session, key, value);
        		break;
        	}
        }
        return session;
	}
	
	
	/**
	 * websocket发送消息
	 * @param session
	 * @param msg
	 */
	public static boolean sendMessage(WebSocketSession session, String msg) {

	    boolean status = false;

        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    session.sendMessage(new TextMessage(msg));
                    status = true;
                } catch (IOException e) {
                    status = false;
                }
            }
        }

        return status;
	}
	
	/**
	 * websocke根据uidt发送消息
	 * @param uid
	 * @param msg
	 */
	public static boolean sendMessage(String uid, String msg) {

        boolean status = false;
		WebSocketSession session = null;

		Iterator<WebSocketSession> it = sessions.iterator();
        while (it.hasNext()) {
        	session = it.next();
        	if (session.getAttributes().get("uid").equals(uid)) {
                status = sendMessage(session, msg);
        		break;
        	}
        }

        return status;
	}
	
	/**
	 * 关闭websocket连接
	 * @param session
	 */
	public static void closeSession(WebSocketSession session) {
		try {
	    	if (session != null && session.isOpen())
	    		session.close();
            GlobalVars.removeSession(session);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据uid关闭关闭websocket连接
	 * @param uid
	 */
	public static void closeSession(String uid) {
		Iterator<WebSocketSession> it = sessions.iterator();
        while (it.hasNext()) {
        	WebSocketSession session = it.next();
        	if (session.getAttributes().get("uid").equals(uid)) {
        		closeSession(session);
        		break;
        	}
        }
	}
}
