package com.example.demo.service;

import com.example.demo.schema.SchemaLoader;
import com.example.demo.schema.TableEmbedding;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InMemorySchemaEmbeddingService {

    private final SchemaLoader schemaLoader;
    private final EmbeddingService embeddingService;

    private final List<TableEmbedding> tableEmbeddings = new ArrayList<>();

    public InMemorySchemaEmbeddingService(SchemaLoader schemaLoader,
                                          EmbeddingService embeddingService) {
        this.schemaLoader = schemaLoader;
        this.embeddingService = embeddingService;
        buildIndex();
    }

    private void buildIndex() {

        tableEmbeddings.clear();

        Map<String, String> descriptions =
                schemaLoader.loadTableDescriptions();

        for (Map.Entry<String, String> entry : descriptions.entrySet()) {

            String tableName = entry.getKey();
            String schemaText = entry.getValue();

            List<Double> embedding =
                    embeddingService.createEmbedding(schemaText);

            tableEmbeddings.add(
                    new TableEmbedding(
                            tableName,
                            schemaText,
                            embedding
                    )
            );
        }

        System.out.println("In-memory schema embeddings built: " + tableEmbeddings.size());
    }

    public List<TableEmbedding> getTableEmbeddings() {
        return tableEmbeddings;
    }
}