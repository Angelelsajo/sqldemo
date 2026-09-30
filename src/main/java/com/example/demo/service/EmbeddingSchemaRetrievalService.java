package com.example.demo.service;

import com.example.demo.schema.TableEmbedding;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.*;

@Service
public class EmbeddingSchemaRetrievalService {
    private record ScoredTable(String tableName, double score) {}

    private final EmbeddingService embeddingService;
    private final InMemorySchemaEmbeddingService indexService;


    public EmbeddingSchemaRetrievalService(EmbeddingService embeddingService,
                                           InMemorySchemaEmbeddingService indexService
    ) {
        this.embeddingService = embeddingService;
        this.indexService = indexService;
    }
    private double cosineSimilarity(List<Double> a, List<Double> b) {

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    public List<String> getRelevantTables(String question, int topK) {

        System.out.println("Question = " + question);

        List<String> tokens = Arrays.stream(
                question.toLowerCase()
                        .replaceAll("[^a-z0-9 ]", " ")
                        .split("\\s+")
        ).toList();

        List<Double> questionEmbedding =
                embeddingService.createEmbedding(question);

        List<ScoredTable> scoredTables = indexService.getTableEmbeddings()
                .stream()
                .map(tableEmbedding -> new ScoredTable(
                        tableEmbedding.getTableName(),
                        cosineSimilarity(
                                questionEmbedding,
                                tableEmbedding.getEmbedding()
                        )
                ))
                .sorted(Comparator.comparingDouble(ScoredTable::score).reversed())
                .toList();

        System.out.println("Table scores = " + scoredTables);

        double bestScore = scoredTables.get(0).score();

        Set<String> selectedTables = new LinkedHashSet<>();

        // 1. First add exact/plural keyword table matches
        for (ScoredTable table : scoredTables) {
            String tableName = table.tableName().toLowerCase();

            for (String token : tokens) {
                if (matchesTableName(token, tableName)) {
                    selectedTables.add(table.tableName());
                }
            }
        }

        // 2. Then add embedding-based matches
        for (ScoredTable table : scoredTables) {
            if (table.score() >= bestScore - 0.05) {
                selectedTables.add(table.tableName());
            }
        }

        List<String> relevantTables = selectedTables.stream()
                .limit(topK)
                .toList();

        System.out.println("Embedding relevant tables = " + relevantTables);

        return relevantTables;
    }
    private boolean matchesTableName(String token, String tableName) {

        return token.equals(tableName)
                || (token + "s").equals(tableName)
                || token.equals(tableName + "s");
    }
}