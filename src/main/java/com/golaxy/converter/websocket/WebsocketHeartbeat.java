package com.golaxy.converter.websocket;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Iterator;

import static com.golaxy.converter.websocket.WebsocketHandler.sessions;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * websocket心跳包
 * 由于tomcat前加了nginx反向代理后，websockt连接只要一小会没有数据传输，nginx就会把websocket连接关闭
 * 通过添加定时器任务每隔5秒向所有已连接的websocket客户端发送一条数据，保证连接不被nginx关闭
 * 定时器依赖: aopalliance-1.0.jar
 */
@Component
public class WebsocketHeartbeat {
	@Scheduled(cron="0/5 * * * * ? ")  //间隔5秒执行 
	public void heartBeat() {
		Iterator<WebSocketSession> it = sessions.iterator();
		while (it.hasNext()) {
			WebSocketSession session = it.next();
			String uid = (String) session.getAttributes().get("uid");
			SessionHandler.sendMessage(session, "{\"heartbeat\":\""+uid+"\"}");
		}
	}
}
