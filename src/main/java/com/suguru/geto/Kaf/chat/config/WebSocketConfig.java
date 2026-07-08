package com.suguru.geto.Kaf.chat.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // configure Message Broker
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");         // prefix for client to hit to send messages
        registry.setUserDestinationPrefix("/user");  // this one to send message to specific user
    }

    // stomp endpoints configure
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp")              // server endpoint to connect to stomp side of websocket
                .setAllowedOrigins("http://localhost:8080")
                .withSockJS();                          // for the browser not supporting websocket
    }
}
