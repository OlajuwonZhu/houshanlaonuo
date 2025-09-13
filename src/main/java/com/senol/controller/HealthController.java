package com.senol.controller;

import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @GetMapping
    public ResponseUtil<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // 基本状态
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("service", "SENOL Backend Service");
        
        // 应用信息
        Map<String, Object> application = new HashMap<>();
        if (buildProperties != null) {
            application.put("name", buildProperties.getName());
            application.put("version", buildProperties.getVersion());
            application.put("buildTime", buildProperties.getTime());
        } else {
            application.put("name", "senol-backend");
            application.put("version", "1.0.0");
            application.put("buildTime", "unknown");
        }
        health.put("application", application);
        
        // 数据库连接检查
        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            database.put("status", "UP");
            database.put("database", connection.getMetaData().getDatabaseProductName());
            database.put("url", connection.getMetaData().getURL());
        } catch (Exception e) {
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
        }
        health.put("database", database);
        
        // 系统信息
        Map<String, Object> system = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("totalMemory", runtime.totalMemory());
        system.put("freeMemory", runtime.freeMemory());
        system.put("maxMemory", runtime.maxMemory());
        system.put("availableProcessors", runtime.availableProcessors());
        health.put("system", system);
        
        return ResponseUtil.success(health);
    }

    @GetMapping("/simple")
    public Map<String, Object> simpleHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("message", "Service is running");
        return health;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
