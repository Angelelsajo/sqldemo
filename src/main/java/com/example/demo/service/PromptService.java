package com.example.demo.service;

import com.example.demo.schema.ForeignKeyRelation;
import com.example.demo.schema.SchemaCache;
import com.example.demo.schema.TableSchema;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PromptService {

    private final SchemaCache schemaCache;

    public PromptService(SchemaCache schemaCache) {
        this.schemaCache = schemaCache;
    }

    public String buildPrompt(String question, List<String> tableNames) {

        Map<String, TableSchema> schema = schemaCache.getSchema();

        StringBuilder schemaText = new StringBuilder();

        for (String tableName : tableNames) {
            TableSchema table = schema.get(tableName);

            if (table != null) {
                schemaText.append("Table: ")
                        .append(table.getTableName())
                        .append("\nColumns: ")
                        .append(String.join(", ", table.getColumns()))
                        .append("\n\n");
            }
        }

        StringBuilder relationshipText = new StringBuilder();

        for (ForeignKeyRelation fk : schemaCache.getForeignKeys()) {
            if (tableNames.contains(fk.getSourceTable())
                    && tableNames.contains(fk.getTargetTable())) {

                relationshipText.append(fk.getSourceTable())
                        .append(".")
                        .append(fk.getSourceColumn())
                        .append(" -> ")
                        .append(fk.getTargetTable())
                        .append(".")
                        .append(fk.getTargetColumn())
                        .append("\n");
            }
        }

        if (relationshipText.isEmpty()) {
            relationshipText.append("No relationships provided.\n");
        }

        return """
                You are an expert PostgreSQL SQL generator.

                Rules:
                1. Use only the given tables and columns.
                2. Use only the given relationships for JOINs.
                3. Do not invent tables or columns.
                4. If a JOIN is needed, use the Relationships section.
                5. Return only SQL.
                6. Do not explain.
                7. Do not use markdown.
                8. Do not wrap SQL in ```sql or ```.
                9. If using CTEs, every selected column must come from an alias defined in the FROM clause.
                10. Use PostgreSQL syntax only.

                Tables:
                %s

                Relationships:
                %s

                Question:
                %s

                SQL:
                """.formatted(
                schemaText.toString(),
                relationshipText.toString(),
                question
        );
    }
}