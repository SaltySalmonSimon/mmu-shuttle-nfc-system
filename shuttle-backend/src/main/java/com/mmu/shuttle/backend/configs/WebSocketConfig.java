package com.mmu.shuttle.backend.configs;

import com.mmu.shuttle.backend.exceptions.WebSocketExceptionHandler;
import com.mmu.shuttle.backend.securities.JwtService;

import io.jsonwebtoken.ExpiredJwtException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // channel prefix
        config.enableSimpleBroker("/routes", "/occupancy");

        // message broker prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.setErrorHandler(new WebSocketExceptionHandler());
        // tcp handshake url
        registry.addEndpoint("/ws-endpoint").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            String email = jwtService.extractUsername(token);

                            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                            accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities()));

                        } catch (ExpiredJwtException e) {
                            throw new AccessDeniedException("JWT_EXPIRED");
                        } catch (Exception e) {
                            throw new AccessDeniedException("JWT_INVALID");
                        }
                    } 
                }

                else if (StompCommand.SEND.equals(accessor.getCommand())) {
                    if (accessor.getUser() == null) {
                        throw new AccessDeniedException("Only authenticated drivers can send updates");
                    }
                }

                return message;
            }
        });
    }
}