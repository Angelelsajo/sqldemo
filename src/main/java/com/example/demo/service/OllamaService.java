package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OllamaService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateSql(String prompt) {

        String url = "http://localhost:11434/api/generate";

        Map<String, Object> request = Map.of(
                "model", "qwen2.5-coder:3b",
                "prompt", prompt,
                "stream", false,
                "options", Map.of(
                        "temperature", 0,
                        "num_predict", 800
                )
        );

        Map response = restTemplate.postForObject(url, request, Map.class);

        String sql = response.get("response").toString();

        return cleanSql(sql);
    }

    private String cleanSql(String sql) {
        return sql
                .replace("```sql", "")
                .replace("```", "")
                .trim();
    }

}