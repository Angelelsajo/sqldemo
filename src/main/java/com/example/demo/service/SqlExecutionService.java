package com.example.demo.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SqlExecutionService {

    private final JdbcTemplate jdbcTemplate;

    public SqlExecutionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeSelectQuery(String sql) {

        String normalizedSql = sql
                .trim()
                .toLowerCase();

        if (containsBlockedKeyword(normalizedSql)) {
            throw new RuntimeException("Only read-only queries are allowed");
        }

        return jdbcTemplate.queryForList(sql);
    }

    private boolean containsBlockedKeyword(String sql) {

        String[] blockedKeywords = {
                "insert",
                "update",
                "delete",
                "drop",
                "alter",
                "truncate",
                "create",
                "replace",
                "merge",
                "grant",
                "revoke",
                "commit",
                "rollback",
                "vacuum",
                "analyze",
                "call",
                "execute"
        };

        for (String keyword : blockedKeywords) {
            if (sql.matches(".*\\b" + keyword + "\\b.*")) {
                return true;
            }
        }

        return false;
    }
}