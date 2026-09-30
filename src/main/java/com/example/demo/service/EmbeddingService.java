package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Double> createEmbedding(String text) {

        String url = "http://localhost:11434/api/embeddings";

        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text",
                "prompt", text
        );

        Map response = restTemplate.postForObject(url, request, Map.class);

        return (List<Double>) response.get("embedding");
    }
}