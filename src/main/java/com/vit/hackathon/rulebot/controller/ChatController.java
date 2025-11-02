package com.vit.hackathon.rulebot.controller;

import com.vit.hackathon.rulebot.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    // This connects our Controller to the "brain"
    @Autowired
    private RAGService ragService;

    /**
     * This method loads our main chat page.
     * It maps to the "chat.html" file we will create next.
     */
    @GetMapping("/")
    public String chatPage(Model model) {
        // We can add "chat history" here later if we have time
        return "chat"; // This tells Spring to find "chat.html"
    }

    /**
     * This method handles the user's question.
     * When the user clicks "Send", the form will POST to this "/ask" URL.
     */
    @PostMapping("/ask")
    public String handleAsk(
            @RequestParam("query") String userQuery, // The user's question from the form
            Model model // The object to send data back to the HTML
    ) {
        
        // 1. Send the question to the "brain" and get an answer
        String botResponse = ragService.askRuleBot(userQuery);

        // 2. Add the question and answer to the model
        model.addAttribute("userQuery", userQuery);
        model.addAttribute("botResponse", botResponse);

        // 3. Send the user back to the same chat page to see the new answer
        return "chat";
    }
}

