package com.golaxy.converter.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * websocket请求拦截器
 */
public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {
	
	private final static Logger logger = LoggerFactory.getLogger(HandshakeInterceptor.class);
  
    @Override  
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        //logger.info("[websocket]: Before Handshake");
        return super.beforeHandshake(request, response, wsHandler, attributes);  
    }  
  
    @Override  
    public void afterHandshake(ServerHttpRequest request,  
            ServerHttpResponse response, WebSocketHandler wsHandler,  
            Exception ex) {  
        //logger.info("[websocket]: After Handshake");
        super.afterHandshake(request, response, wsHandler, ex);  
    }
}