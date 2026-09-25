package com.ai.springAiDemo.controller;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
public class EmbeddingController {

    private EmbeddingModel embeddingModel;

    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/embed")
    public float[] generateEmbeddings(@RequestParam String text) {
        // Generates the vector embeddings locally without any API calls
        System.out.println(embeddingModel.embedForResponse(Collections.singletonList(text)).getMetadata().getModel());
        return embeddingModel.embed(text);
    }



}
