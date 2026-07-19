package com.suguru.geto.Kaf.chat.config;


import com.suguru.geto.Kaf.chat.payload.ChatMessageDto;
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
        // destination prefix for messages -> means they are address to DELIVER  ||   used when u have to deliever a message
        //  u can observe it on ur own in file chatController -> where to deliver a msg received by client we save then
        //      deliver to using of the address starter above -> do go observe on ur own
        /** {@link com.suguru.geto.Kaf.chat.controller.ChatController#sendMessage(ChatMessageDto)} */



        // /topic -> broadcast to all users  (thats as per namming only u can set it vice versa for topic and queue)
        // /queue -> send to specific user
        // thing is we want two lanes seprated by /
        registry.setApplicationDestinationPrefixes("/app");         // prefix for client to hit to send messages
        registry.setUserDestinationPrefix("/user");  // this one to send message to specific user
    }

    // stomp endpoints configure
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp")              // server endpoint to connect to stomp side of websocket
//                .setAllowedOrigins("http://localhost:8080")
//                .withSockJS();                          // for the browser not supporting websocket
                .setAllowedOriginPatterns("*");
    }
}
