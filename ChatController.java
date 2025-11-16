package com.vit.hackathon;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private RAGService ragService;

    @PostMapping("/query")
    public String queryChat(@RequestBody ChatMessage chatMessage) {
        return ragService.processQuery(chatMessage.getMessage());
    }

    @GetMapping("/history")
    public List<String> getChatHistory() {
        return ragService.getChatHistory();
    }

    @PostMapping("/translate")
    public String translateText(@RequestBody ChatMessage chatMessage) {
        // Assume translation service
        return "Translated: " + chatMessage.getMessage();
    }
}
