package com.ai.springAiDemo.controller;

import com.ai.springAiDemo.records.MovieDocumentRequest;
import jakarta.validation.Valid;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.XmlMapperBuilderCustomizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@Validated
@RequestMapping("/ai")
public class VectorStoreController {

    private final ObjectMapper objectMapper;

    private final MongoDBAtlasVectorStore vectorStore;

    // Spring AI injects our configured MongoDB Atlas Vector Store here
    public VectorStoreController(MongoDBAtlasVectorStore vectorStore, ObjectMapper objectMapper) {
        this.vectorStore = vectorStore;
        this.objectMapper = objectMapper;
    }

    /**
     * Endpoint to save unstructured text to your MongoDB Cloud Vector Store.
     * Behind the scenes, Spring AI reads this text, reaches out to Ollama
     * to compute vectors, and pushes both the text + vectors into MongoDB Atlas.
     */
    @PostMapping("/ingest")
    public String ingestData(@RequestBody List<@Valid MovieDocumentRequest> requests, InputStream inputStream) {
        Document document = new Document(requests.get(0).getContent(), objectMapper.convertValue(requests.get(0).getMetadata(), Map.class));
        vectorStore.add(List.of(document));

        return objectMapper.writeValueAsString(document);
//
//        return "Successfully uploaded content and generated vector embeddings to MongoDB Atlas cloud!";
    }

    @PostMapping("/ingest/bulk")
    public String ingestBulkData(@RequestBody List<@Valid MovieDocumentRequest> requests) {

        List<Document> documents = requests.stream()
                .map(req ->
                        new Document(req.getContent(), objectMapper.convertValue(req.getMetadata(), Map.class)))
                .toList();

        vectorStore.add(documents);
        return "Successfully ingested " + documents.size() + " documents into your MongoDB vector store!";
    }


    /**
     * Endpoint to execute a similarity search across MongoDB Atlas cloud.
     */
    @GetMapping("/searchFromVectorStore")
    public List<String> search(@RequestParam String query) {
        List<Document> matchingDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(2) // Fetches top 2 most matching chunks
                        .build()
        );

        return matchingDocuments.stream()
                .map(Document::getText)
                .toList();
    }

    @GetMapping("/search-test")
    public List<String> searchTest(@RequestParam String query) {
        List<Document> matchingDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(2)
                        // COMMENT OUT THE FILTER EXPRESSION TEMPORARILY
                        // .filterExpression("category == '" + category + "'")
                        .build()
        );

        return matchingDocuments.stream()
                .map(Document::getText)
                .toList();
    }

}
