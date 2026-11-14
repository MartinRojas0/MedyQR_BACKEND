package com.mediqr.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public HealthResponse health() {
        jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return new HealthResponse("ok");
    }

    public record HealthResponse(String status) {
    }
}
