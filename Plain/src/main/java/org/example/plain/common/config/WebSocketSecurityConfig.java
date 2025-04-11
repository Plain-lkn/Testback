package org.example.plain.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public MessageMatcherDelegatingAuthorizationManager.Builder messageAuthorizationManager() {
        MessageMatcherDelegatingAuthorizationManager.Builder messages = 
                MessageMatcherDelegatingAuthorizationManager.builder();
        
        messages
                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.HEARTBEAT, SimpMessageType.UNSUBSCRIBE, 
                                 SimpMessageType.DISCONNECT).permitAll()
                .simpDestMatchers("/ws/meeting/**").permitAll()
                .anyMessage().permitAll();
        
        return messages;
    }
} 