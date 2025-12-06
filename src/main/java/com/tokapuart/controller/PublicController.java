package com.tokapuart.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    
    @Value("${spring.datasource.url:not-configured}")
    private String databaseUrl;
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Tokapu Art Backend");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Backend funcionando correctamente! 🎉");
        
        // Info del servidor
        Map<String, String> server = new HashMap<>();
        server.put("java_version", System.getProperty("java.version"));
        server.put("os", System.getProperty("os.name"));
        server.put("environment", System.getenv().getOrDefault("RAILWAY_ENVIRONMENT", "development"));
        
        response.put("server", server);
        
        // Estado de la base de datos (sin exponer credenciales)
        Map<String, Object> database = new HashMap<>();
        database.put("connected", !databaseUrl.equals("not-configured"));
        database.put("type", databaseUrl.contains("mysql") ? "MySQL" : "Unknown");
        
        response.put("database", database);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("✅ API funcionando correctamente - Tokapu Art Backend");
    }
    
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}