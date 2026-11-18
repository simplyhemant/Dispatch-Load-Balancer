
package com.dispatchloadbalancer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class TestController {

    @GetMapping(value = "/", produces = "text/html")
    public String testBackend() {

        String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Dispatch Load Balancer</title>

                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            font-family: Arial, sans-serif;
                            text-align: center;
                            margin: 0;
                            padding: 40px 20px;
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            background: #f5f7fa;
                        }

                        .container {
                            background: white;
                            padding: 40px 30px;
                            width: 100%;
                            max-width: 600px;
                            border-radius: 12px;
                            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
                        }

                        h1 {
                            color: #198754;
                            margin-bottom: 10px;
                        }

                        h2 {
                            color: #333;
                            margin-bottom: 15px;
                        }

                        p {
                            color: #666;
                            line-height: 1.6;
                        }

                        .status {
                            display: inline-block;
                            background: #d1e7dd;
                            color: #0f5132;
                            padding: 8px 16px;
                            border-radius: 20px;
                            font-size: 14px;
                            font-weight: bold;
                            margin: 15px 0;
                        }

                        .links {
                            margin-top: 25px;
                        }

                        a {
                            display: block;
                            margin: 12px 0;
                            padding: 13px;
                            color: white;
                            background: #0d6efd;
                            text-decoration: none;
                            border-radius: 7px;
                            transition: background 0.2s;
                        }

                        a:hover {
                            background: #0b5ed7;
                        }

                        .postman {
                            background: #ff6c37;
                        }

                        .postman:hover {
                            background: #e85d2a;
                        }

                        .footer {
                            margin-top: 25px;
                            font-size: 12px;
                            color: #999;
                        }
                    </style>
                </head>

                <body>
                    <div class="container">

                        <h1>Backend is Running!</h1>

                        <h2>Dispatch Load Balancer</h2>

                        <div class="status">
                            Application Status: UP
                        </div>

                        <p>
                            Your Spring Boot application is up and running successfully.
                        </p>

                        <hr>

                        <div class="links">

                            <a href="__BASE_URL__">
                                Live Backend URL
                            </a>

                            <a href="__BASE_URL__/swagger-ui.html" target="_blank">
                                Swagger UI
                            </a>

                            <a href="__BASE_URL__/api-docs" target="_blank">
                                OpenAPI Documentation
                            </a>

                            <a href="https://documenter.getpostman.com/view/39898850/2sBYHbsbjw"
                               class="postman"
                               target="_blank"
                               rel="noopener noreferrer">
                                Postman API Documentation
                            </a>

                        </div>

                        <div class="footer">
                            Dispatch Load Balancer | Spring Boot REST API
                        </div>

                    </div>
                </body>
                </html>
                """.replace("__BASE_URL__", baseUrl);
    }
}
