package com.microservice.citas.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration

public class FeingCLientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        final String BEARER_PREFIX = "Bearer ";


        if (authentication instanceof JwtAuthenticationToken jwt){
            String tokenJwt = jwt.getToken().getTokenValue();

            template.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokenJwt);
        }
    }
}
