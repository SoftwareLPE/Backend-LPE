package com.example.backend_sistema_LPE.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint de handshake STOMP (sin SockJS por ahora).
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijos de salida para tópicos públicos y colas (si se usan).
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefijo para mensajes enviados desde cliente hacia @MessageMapping (si lo agregas después).
        registry.setApplicationDestinationPrefixes("/app");
    }
}
