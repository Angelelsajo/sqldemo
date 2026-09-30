package com.example.demo.schema;

import java.util.List;

public class TableEmbedding {

    private String tableName;
    private String schemaText;
    private List<Double> embedding;

    public TableEmbedding(String tableName, String schemaText, List<Double> embedding) {
        this.tableName = tableName;
        this.schemaText = schemaText;
        this.embedding = embedding;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSchemaText() {
        return schemaText;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }
}
