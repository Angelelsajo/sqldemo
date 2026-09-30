package com.example.demo.schema;

public class ForeignKeyRelation {

    private String sourceTable;
    private String sourceColumn;
    private String targetTable;
    private String targetColumn;

    public ForeignKeyRelation(String sourceTable, String sourceColumn,
                              String targetTable, String targetColumn) {
        this.sourceTable = sourceTable;
        this.sourceColumn = sourceColumn;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public String getTargetColumn() {
        return targetColumn;
    }
}