package com.ai.springAiDemo.controller;

import com.ai.springAiDemo.records.Chunk;
import com.ai.springAiDemo.records.MovieDocumentRequest;
import io.modelcontextprotocol.client.McpSyncClient;
import io.netty.handler.stream.ChunkedStream;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@CrossOrigin(origins = "*")
@RestController
public class chatClientController {


    private final ChatClient chatClient;

    @Value("classpath:prompts/restaurant-system-prompt.txt")
    private Resource restaurantSystemPrompt;

    @Value("classpath:data/restaurants.csv")
    private Resource restaurantData;

    @Value("classpath:prompts/movie-recommend-prompt.txt")
    private Resource moviePrompt;

    public chatClientController(
            ChatClient.Builder chatClientBuilder,
            List<McpSyncClient> mcpSyncClients,
            @Value("classpath:prompts/global-system-prompt.txt") Resource globalSystemPrompt) {
        this.chatClient = chatClientBuilder.defaultTools(SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClients).build())
                                            .defaultSystem(globalSystemPrompt)
                                            .build();
        System.out.println(mcpSyncClients);
    }

    @GetMapping("/chatClient/chat/{message}")
    public String generalChatClient(@PathVariable String message) {
        String response = chatClient.prompt()
                .user(message).call().content();
        return response;
    }

    @GetMapping(value = "/chatClient/ReactivefluxChat" ,  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Chunk> ReactivefluxChatClient(@RequestParam String message) {
        return chatClient.prompt().tools(this)
                .user(message).stream().content().map(text -> new Chunk(text));
    }



//    var chatClient = chatClientBuilder.defaultSystem(
//                    "You are useful assistant and can perform web searches Brave's search API to reply to your questions.")
//            .defaultTools(SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClients).build())
//            .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
//            .build();


    @GetMapping("/chatClient/restaurant/{message}")
    public String chatClient(@PathVariable String message) {

        String response = chatClient.prompt()
                .system(restaurantSystemPrompt)
                .system(restaurantData)
                .user(message).call().content();
        return response;
    }




    @GetMapping("/chatClient/movie")
    public String movieRecommendation(@RequestParam String genre, @RequestParam String language) {

        //using PromptTemplate to pass parameters to the system prompt
//        PromptTemplate template = PromptTemplate.builder()
//                .resource(moviePrompt)
//                .variables(Map.of("genre", genre, "language", language)).build();
//
//        String responseUsingPromptTemplate = chatClient.prompt(template.create()).call().content();


        //using promptSystemSpec to pass parameters to the system prompt
        String response = chatClient.prompt()
                .system(u -> u.text(moviePrompt)
                        .param("genre", genre)
                        .param("language", language))
                .user("Please recommend a movie based on the above criteria.")
                .call().content();


//    return responseUsingPromptTemplate;
        return response;
    }



    @GetMapping("bulkMovieGen-StructuredOutputValidation")
            public  List<MovieDocumentRequest> bulkMovieGenStructuredOutputValidaiton(@RequestParam String actor) {
        //structured output.
        List<MovieDocumentRequest> movieDocumentRequest = chatClient.prompt()
                .system("You are an expert film historian specialized in Indian cinema.Include box office collection in the response.")
                .user("Provide a list of top 5 Hit movies starring the Indian actor " + actor
                        + ". The content must include the high level entire movie story and Include box office collection in the response. ")
                .call()
                .entity(new ParameterizedTypeReference<List<MovieDocumentRequest>>() {}
//                        , spec -> spec
//                        .useProviderStructuredOutput()
//                                .validateSchema()
                        );
//        ollama cloud dont support structured output. :( Need to check local.

        return movieDocumentRequest;
    }


    @GetMapping("movieGen-StructuredOutputValidation")
    @Tool(
            name = "getMovieRecommendation",
            description = "Provides a highly detailed movie recommendation with the input as actor, including the full plot and box office collections."
    )
    public  MovieDocumentRequest movieGenStructuredOutputValidaiton(@RequestParam String actor) {
        //structured output.
        MovieDocumentRequest movieDocumentRequest = chatClient.prompt()
                .system("You are an expert film historian specialized in Indian cinema.")
                .user("Provide a movie recommendation starring the Indian actor " + actor
                        + ". The content must include the high level entire movie story and Include box office collection in the response. ")
                .call()
                .entity(MovieDocumentRequest.class, spec -> spec
//                        .useProviderStructuredOutput()
                                .validateSchema()
                );

//        ollama cloud dont support structured output. :( Need to check local.

        return movieDocumentRequest;
    }

}
