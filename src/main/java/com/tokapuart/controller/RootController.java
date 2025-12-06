package com.tokapuart.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {
    
    @Value("${server.base-url}")
    private String baseUrl;
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Tokapu Art API");
        response.put("status", "✅ ONLINE");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Bienvenido a Tokapu Art Backend - API REST para red social de arte urbano");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("artworks", baseUrl + "/api/artworks");
        endpoints.put("auth_register", baseUrl + "/api/auth/register");
        endpoints.put("auth_login", baseUrl + "/api/auth/login");
        endpoints.put("auth_google", baseUrl + "/api/auth/google");
        endpoints.put("users", baseUrl + "/api/users");
        endpoints.put("status", baseUrl + "/api/public/status");
        
        response.put("endpoints", endpoints);
        response.put("documentation", "Tokapu Art - Red Social de Arte Urbano Peruano");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "tokapuart-backend");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}