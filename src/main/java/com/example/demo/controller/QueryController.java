package com.example.demo.controller;
import com.example.demo.service.EmbeddingSchemaRetrievalService;
import com.example.demo.dto.QueryRequest;
import com.example.demo.service.OllamaService;
import com.example.demo.service.PromptService;
import com.example.demo.service.SqlExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class QueryController {
    private final EmbeddingSchemaRetrievalService embeddingSchemaRetrievalService;
    private final SqlExecutionService sqlExecutionService;
    private final PromptService promptService;
    private final OllamaService ollamaService;

    public QueryController(
                           PromptService promptService,
                           OllamaService ollamaService,
                           EmbeddingSchemaRetrievalService embeddingSchemaRetrievalService,
                           SqlExecutionService sqlExecutionService) {


        this.promptService = promptService;
        this.ollamaService = ollamaService;
        this.embeddingSchemaRetrievalService = embeddingSchemaRetrievalService;
        this.sqlExecutionService = sqlExecutionService;

    }

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody QueryRequest request) {
        try {
            String question = request.getQuestion();

            List<String> tables =
                    embeddingSchemaRetrievalService.getRelevantTables(question, 4);

            String prompt =
                    promptService.buildPrompt(question, tables);

            String sql =
                    ollamaService.generateSql(prompt);
            System.out.println("Generated SQL = " + sql);
            List<Map<String, Object>> results =
                    sqlExecutionService.executeSelectQuery(sql);
            return ResponseEntity.ok(Map.of(
                    "sql", sql,
                    "results", results
            ));

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));

        }
    }
}

