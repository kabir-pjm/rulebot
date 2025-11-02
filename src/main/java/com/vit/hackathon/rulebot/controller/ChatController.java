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
@SessionAttributes("chatHistory") 
public class ChatController {

    @Autowired
    private RAGService ragService;

    @ModelAttribute("chatHistory")
    public List<ChatMessage> getChatHistory() {
        return new ArrayList<>();
    }

    // --- MODIFIED ---
    @GetMapping("/")
    public String chatPage(@ModelAttribute("chatHistory") List<ChatMessage> chatHistory, Model model) {
        model.addAttribute("chatHistory", chatHistory);
        // Add a default selected language for the first page load
        model.addAttribute("selectedLanguage", "English"); 
        return "chat"; 
    }

    // --- MODIFIED ---
    @PostMapping("/ask")
    public String handleAsk(
            @RequestParam("query") String userQuery,
            @RequestParam("language") String language, // <-- NEW: Get language from form
            @ModelAttribute("chatHistory") List<ChatMessage> chatHistory, 
            Model model
    ) {
        
        chatHistory.add(new ChatMessage("user", userQuery));

        // Pass the new language parameter to the "brain"
        String botResponse = ragService.askRuleBot(chatHistory, language); // <-- MODIFIED

        chatHistory.add(new ChatMessage("bot", botResponse));

        model.addAttribute("chatHistory", chatHistory);
        model.addAttribute("selectedLanguage", language); // <-- NEW: Add language back to model
        
        return "chat";
    }
}