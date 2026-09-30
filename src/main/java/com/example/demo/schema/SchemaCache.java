package com.example.demo.schema;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SchemaCache {

    private final SchemaLoader schemaLoader;

    private Map<String, TableSchema> cachedSchema;
    private List<ForeignKeyRelation> foreignKeys;

    public SchemaCache(SchemaLoader schemaLoader) {
        this.schemaLoader = schemaLoader;
        this.cachedSchema = schemaLoader.loadSchema();
        this.foreignKeys = schemaLoader.loadForeignKeys();
    }

    public Map<String, TableSchema> getSchema() {
        return cachedSchema;
    }

    public List<ForeignKeyRelation> getForeignKeys() {
        return foreignKeys;
    }

    public void refresh() {
        this.cachedSchema = schemaLoader.loadSchema();
        this.foreignKeys = schemaLoader.loadForeignKeys();
    }
}