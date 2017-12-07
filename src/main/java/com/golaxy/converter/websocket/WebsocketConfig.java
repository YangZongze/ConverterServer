package com.golaxy.converter.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * websocket配置
 */
@Configuration  
@EnableWebMvc  
@EnableWebSocket  
public class WebsocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer{  
  
    @Override  
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {  
        registry.addHandler(systemWebSocketHandler(),"/websocket").addInterceptors(new HandshakeInterceptor()).setAllowedOrigins("*");
        registry.addHandler(systemWebSocketHandler(),"/websocket/sockjs").addInterceptors(new HandshakeInterceptor()).withSockJS();  
        //registry.addHandler(systemWebSocketHandler(),"/webSocketServer/sockjs").setAllowedOrigins("*").withSockJS();  
        //registry.addHandler(systemWebSocketHandler(),"/webSocketServer").addInterceptors(new WebSocketHandshakeInterceptor());  
        //registry.addHandler(systemWebSocketHandler(), "/sockjs/webSocketServer").addInterceptors(new WebSocketHandshakeInterceptor()).withSockJS();  
        //registry.addHandler(systemWebSocketHandler(), "/webSocketServer/sockjs").withSockJS();  
        //registry.addHandler(systemWebSocketHandler(),"/ws").addInterceptors(new WebSocketHandshakeInterceptor()); 
        //registry.addHandler(systemWebSocketHandler(), "/ws/sockjs").addInterceptors(new WebSocketHandshakeInterceptor()).withSockJS();  
    }  
      
    @Bean
    public WebSocketHandler systemWebSocketHandler(){  
        return new WebsocketHandler();  
    } 
}
