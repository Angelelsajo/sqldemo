package com.example.demo.schema;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SchemaLoader {

    private final JdbcTemplate jdbcTemplate;

    public SchemaLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public List<ForeignKeyRelation> loadForeignKeys() {

        String sql = """
        SELECT
            c.conrelid::regclass::text AS source_table,
            a.attname AS source_column,
            c.confrelid::regclass::text AS target_table,
            af.attname AS target_column
        FROM pg_constraint c
        JOIN pg_attribute a
            ON a.attrelid = c.conrelid
           AND a.attnum = ANY(c.conkey)
        JOIN pg_attribute af
            ON af.attrelid = c.confrelid
           AND af.attnum = ANY(c.confkey)
        WHERE c.contype = 'f'
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new ForeignKeyRelation(
                        rs.getString("source_table"),
                        rs.getString("source_column"),
                        rs.getString("target_table"),
                        rs.getString("target_column")
                )
        );
    }

    public Map<String, TableSchema> loadSchema() {

        Map<String, List<String>> tableColumns = new HashMap<>();

        // STEP 1: Get all tables
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                String.class
        );

        // STEP 2: Get columns for each table
        for (String table : tables) {

            List<String> columns = jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_name = ?",
                    String.class,
                    table
            );

            tableColumns.put(table, columns);
        }

        // STEP 3: Convert to schema objects
        Map<String, TableSchema> schemaMap = new HashMap<>();

        for (var entry : tableColumns.entrySet()) {
            schemaMap.put(
                    entry.getKey(),
                    new TableSchema(entry.getKey(), entry.getValue())
            );
        }

        return schemaMap;
    }
    public Map<String, String> loadTableDescriptions() {

        Map<String, TableSchema> schemaMap = loadSchema();
        List<ForeignKeyRelation> foreignKeys = loadForeignKeys();

        Map<String, String> descriptions = new HashMap<>();

        for (TableSchema tableSchema : schemaMap.values()) {

            String tableName = tableSchema.getTableName();
            List<String> columns = tableSchema.getColumns();

            StringBuilder description = new StringBuilder();

            description.append("Table: ").append(tableName).append("\n");
            description.append("Columns: ").append(String.join(", ", columns)).append("\n");

            description.append("Relationships: ");

            boolean hasRelation = false;

            for (ForeignKeyRelation fk : foreignKeys) {

                if (fk.getSourceTable().equals(tableName)) {
                    description.append(tableName)
                            .append(".")
                            .append(fk.getSourceColumn())
                            .append(" references ")
                            .append(fk.getTargetTable())
                            .append(".")
                            .append(fk.getTargetColumn())
                            .append(". ");

                    hasRelation = true;
                }

                if (fk.getTargetTable().equals(tableName)) {
                    description.append(tableName)
                            .append(".")
                            .append(fk.getTargetColumn())
                            .append(" is referenced by ")
                            .append(fk.getSourceTable())
                            .append(".")
                            .append(fk.getSourceColumn())
                            .append(". ");

                    hasRelation = true;
                }
            }

            if (!hasRelation) {
                description.append("No foreign key relationships found.");
            }

            description.append("\n");

            description.append("Sample data: ");
            description.append(loadSampleData(tableName, columns));

            descriptions.put(tableName, description.toString());
        }
        System.out.println("===== TABLE DESCRIPTIONS =====");

        for (Map.Entry<String, String> entry : descriptions.entrySet()) {
            System.out.println("Table name: " + entry.getKey());
            System.out.println(entry.getValue());
            System.out.println("-----------------------------");
        }

        return descriptions;
    }
    private String loadSampleData(String tableName, List<String> columns) {

        try {
            String sql = "SELECT * FROM " + tableName + " LIMIT 3";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            if (rows.isEmpty()) {
                return "No sample data available.";
            }

            StringBuilder sample = new StringBuilder();

            for (Map<String, Object> row : rows) {
                for (String column : columns) {
                    Object value = row.get(column);

                    if (value != null) {
                        sample.append(column)
                                .append("=")
                                .append(value)
                                .append(", ");
                    }
                }
                sample.append(" | ");
            }

            return sample.toString();

        } catch (Exception e) {
            return "Could not load sample data.";
        }
    }
}