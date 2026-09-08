package com.microservice.users.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenApi(){
            return new OpenAPI()
                    .servers(List.of(new Server().url("http://localhost:8080").description(" API GATEWAY")))
                    .info(new Info()
                    .title("OPEN API MCSV USERS")
                    .version("0.0.1")
                    .description("Open API MCSV USERS")
                    .termsOfService("http://swagger.io/terms/")
                    .license(new License().name("Apache 2.0").url("http://springdoc.org"))

            );
    }
}
