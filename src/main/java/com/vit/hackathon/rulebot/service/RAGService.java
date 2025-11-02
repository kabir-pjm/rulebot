package com.vit.hackathon.rulebot.service;

import com.vit.hackathon.rulebot.model.ChatMessage; // Import our new class
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

            Use the provided chat history to understand the user's follow-up questions.

            Here are the relevant rules:
            ---
            {context}
            ---
            """;

    @Autowired
    public RAGService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // This is the main change. We now accept the *entire* history.
    public String askRuleBot(List<ChatMessage> chatHistory) {

        // 1. Get the user's *latest* question
        String userQuery = chatHistory.get(chatHistory.size() - 1).content();

        // 2. Retrieve relevant rules based on that question
        List<Document> relevantDocuments = this.vectorStore.similaritySearch(userQuery);
        String context = relevantDocuments.stream()
                .map(doc -> doc.getText())
                .collect(Collectors.joining("\n"));

        // 3. Create the System Message with the rules
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(systemPromptTemplate);
        Message systemMessage = promptTemplate.createMessage(Map.of("context", context));

        // 4. Convert our simple ChatMessage list into Spring AI's Message list
        List<Message> allMessages = chatHistory.stream()
                .map(msg -> (Message) new UserMessage(msg.content())) // We'll treat all as "user" for simplicity
                .collect(Collectors.toList());

        // 5. Add our system prompt to the *beginning* of the history
        allMessages.add(0, systemMessage); 

        // 6. Send the whole conversation to the AI
        Prompt promptToSend = new Prompt(allMessages);

        String aiResponse = chatClient.prompt(promptToSend)
                                      .call()
                                      .content();

        return aiResponse;
    }
}