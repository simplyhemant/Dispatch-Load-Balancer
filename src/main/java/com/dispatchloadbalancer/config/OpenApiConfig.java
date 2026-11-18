package com.dispatchloadbalancer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dispatchLoadBalancerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dispatch Load Balancer API")
                        .description("RESTful API service for optimizing delivery order allocation across vehicle fleets with capacity constraints, priority scheduling, and Haversine distance minimization.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dispatch Engineering Team")
                                .email("engineering@dispatchloadbalancer.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
