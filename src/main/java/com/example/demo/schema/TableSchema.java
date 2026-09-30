package com.example.demo.schema;

import java.util.List;

public class TableSchema {

    private String tableName;
    private List<String> columns;

    public TableSchema(String tableName, List<String> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }
}