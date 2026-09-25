package com.ai.springAiDemo.controller;

import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.logging.Logger;

@RestController
public class chatModelController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(chatModelController.class);
    private final ObjectMapper objectMapper;
    private ChatModel chatModel;

    Logger logger = Logger.getLogger(chatModelController.class.getName());
    //constructor
    public chatModelController(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/chatModel")
    public ChatModel getChatModel() {
        return chatModel;
    }

    @GetMapping("/chatModelName")
    public String getChatModelName() {
        return chatModel.getOptions().getModel();
    }

    @GetMapping("/chat/{message}")
    public ResponseEntity<String> chat(@PathVariable String message) {
        log.info("Sending message to chat model: {}", message);
        String response = chatModel.call(message);
        log.info("Received response from chat model: {}", response);
//        change content type to application/json

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(response);

    }


    @GetMapping("/usingChatResponse/{message}")
    public ResponseEntity<Map<String, Object>> usingChatResponse(@PathVariable String message) {
        log.info("Sending message to chat model: {}", message);

        ChatResponse chatResponse = chatModel.call(new Prompt(message));

        log.info("Received response from chat model: {}", objectMapper.writeValueAsString(chatResponse));
        String responseText = chatResponse.getResult().getOutput().getText();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("response",responseText));
    }


    @GetMapping("/chat")
    public String chatDefault() {
        String defaultMessage = "tell me about spring ai in 1 line";
        log.info("Sending default message to chat model: {}", defaultMessage);
        String response = chatModel.call(defaultMessage);
        log.info("Received response from chat model: {}", response);
        return response;
    }
}
