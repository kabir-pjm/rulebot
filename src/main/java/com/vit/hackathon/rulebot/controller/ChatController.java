package com.vit.hackathon.rulebot.controller;

import com.vit.hackathon.rulebot.model.ChatMessage;
import com.vit.hackathon.rulebot.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
// This tells Spring to store the "chatHistory" object in the user's session
@SessionAttributes("chatHistory") 
public class ChatController {

    @Autowired
    private RAGService ragService;

    // This is a helper method to add an object to the session
    @ModelAttribute("chatHistory")
    public List<ChatMessage> getChatHistory() {
        return new ArrayList<>();
    }

    // When the user first lands on the page
    @GetMapping("/")
    public String chatPage(@ModelAttribute("chatHistory") List<ChatMessage> chatHistory, Model model) {
        model.addAttribute("chatHistory", chatHistory);
        return "chat"; // This maps to "chat.html"
    }

    // When the user clicks "Send"
    @PostMapping("/ask")
    public String handleAsk(
            @RequestParam("query") String userQuery,
            // This magic annotation gets the history from the session
            @ModelAttribute("chatHistory") List<ChatMessage> chatHistory, 
            Model model
    ) {

        // 1. Add the user's new question to the history
        chatHistory.add(new ChatMessage("user", userQuery));

        // 2. Send the *entire* history to the brain
        String botResponse = ragService.askRuleBot(chatHistory);

        // 3. Add the bot's response to the history
        chatHistory.add(new ChatMessage("bot", botResponse));

        // 4. Send the updated history back to the HTML page
        model.addAttribute("chatHistory", chatHistory);

        return "chat";
    }
}