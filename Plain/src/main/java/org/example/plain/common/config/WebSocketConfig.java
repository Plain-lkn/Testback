package org.example.plain.common.config;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.chat.handler.ChatWebSocketHandler;
import org.example.plain.domain.meeting.handler.MeetingWebSocketHandler;
import org.example.plain.domain.meeting.interceptor.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final MeetingWebSocketHandler meetingWebSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(meetingWebSocketHandler, "/ws/meeting/{roomId}")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");

        registry.addHandler(chatWebSocketHandler, "/ws/chat/{chatId}")
                //.addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }
} 