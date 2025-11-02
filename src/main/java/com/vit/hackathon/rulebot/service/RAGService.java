package com.vit.hackathon.rulebot.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RAGService {

    private final ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

    private final String systemPromptTemplate = """
            You are a helpful 'RuleBot'. Your job is to answer questions about company policies.
            You must answer questions based *only* on the business rules provided in the context.
            If the answer is not in the provided rules, you MUST say:
            "I'm sorry, I don't have information on that topic."

            Here are the relevant rules:
            ---
            {context}
            ---
            """;

    @Autowired
    public RAGService(ChatClient.Builder chatClientBuilder) {
        // This builds the client and connects it to Ollama
        this.chatClient = chatClientBuilder.build();
    }

    public String askRuleBot(String userQuery) {

        // 1. Retrieve
        List<Document> relevantDocuments = this.vectorStore.similaritySearch(userQuery);

        // 2. Augment
        // This is the correct way to get the content
        // 2. Augment
// This is the correct method name
String context = relevantDocuments.stream()
.map(doc -> doc.getText())  // <-- THIS IS THE CORRECT LINE
.collect(Collectors.joining("\n"));

        // 3. Generate
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(systemPromptTemplate);
        Message systemMessage = promptTemplate.createMessage(Map.of("context", context));
        UserMessage userMessage = new UserMessage(userQuery);

        Prompt promptToSend = new Prompt(List.of(systemMessage, userMessage));

        // 4. Call the AI
        // This is the new, correct way to call the ChatClient
        String aiResponse = chatClient.prompt(promptToSend)
                                      .call()
                                      .content();

        return aiResponse;
    }
}