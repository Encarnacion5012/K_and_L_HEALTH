package com.microservice.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Rutas públicas de Swagger y Webjars
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/users-service/v3/api-docs",
                                "/users-service/v3/api-docs/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Rutas públicas de API (con barra inicial obligatoria)
                        .pathMatchers(HttpMethod.POST, "/api/pacientes/create").permitAll()

                        // Todo lo demás requiere autenticación
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .build();
    }
}