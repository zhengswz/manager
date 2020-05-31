package com.swz.manager.config;

import com.swz.manager.security.AuthoritiesConstants;

import java.security.Principal;
import java.util.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.*;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import io.github.jhipster.config.JHipsterProperties;

import javax.servlet.http.HttpSession;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

    public static final String IP_ADDRESS = "IP_ADDRESS";

    private final JHipsterProperties jHipsterProperties;

    public WebsocketConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowedOrigins = Optional.ofNullable(jHipsterProperties.getCors().getAllowedOrigins()).map(origins -> origins.toArray(new String[0])).orElse(new String[0]);
        registry.addEndpoint("/websocket/tracker")
            .setHandshakeHandler(defaultHandshakeHandler())
            .setAllowedOrigins(allowedOrigins) .addInterceptors(httpSessionHandshakeInterceptor());
//            .withSockJS()
//            .setInterceptors(httpSessionHandshakeInterceptor());
    }

    private HttpSession getSession(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
            return serverRequest.getServletRequest().getSession(false);
        }
        return null;
    }

    @Bean
    public HandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HandshakeInterceptor() {

            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                System.out.println("握手之前");
                HttpSession session = getSession(request);
                if (request instanceof ServletServerHttpRequest) {
                    System.out.println("握手之前-1");
                    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                    attributes.put(IP_ADDRESS, servletRequest.getRemoteAddress());
                }
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
                System.out.println("握手之后");
            }
        };
    }

    private DefaultHandshakeHandler defaultHandshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//                request.getHeaders()

                Principal principal = request.getPrincipal();
                System.out.println("####################");
                if (principal == null) {
                    System.out.println("是null");
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
//                    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
//                    principal = new AnonymousAuthenticationToken("WebsocketConfiguration", "anonymous", authorities);
                    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
                    principal = new UsernamePasswordAuthenticationToken("admin","admin",authorities);
                }
                System.out.println("####################");
                System.out.println(principal.getName());
                System.out.println(principal.toString());
                return principal;
            }
        };
    }
}
