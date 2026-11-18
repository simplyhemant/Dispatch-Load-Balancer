
package com.dispatchloadbalancer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class TestController {

    @GetMapping(produces = "text/html")
    public String testBackend() {

        String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Dispatch Load Balancer</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            text-align: center;
                            margin-top: 100px;
                            background: #f5f5f5;
                        }
                        .container {
                            background: white;
                            padding: 35px;
                            margin: auto;
                            max-width: 550px;
                            border-radius: 10px;
                        }
                        h1 { color: #198754; }
                        a {
                            display: block;
                            margin: 15px;
                            color: #0d6efd;
                            text-decoration: none;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Backend is Running!</h1>
                        <h2>Dispatch Load Balancer</h2>
                        <p>Spring Boot application is up and running successfully.</p>
                        <hr>
                        <a href="%s">Live Backend URL</a>
                        <a href="%s/swagger-ui.html">Swagger UI</a>
                        <a href="%s/api-docs">API Documentation</a>
                    </div>
                </body>
                </html>
                """.formatted(baseUrl, baseUrl, baseUrl);
    }
}
