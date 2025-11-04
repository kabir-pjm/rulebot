package com.vit.hackathon.rulebot.service;

import com.vit.hackathon.rulebot.model.ChatMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.*;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RAGService {

        @Autowired
        private SimpleTranslationService ser;

        private final ChatClient chatClient;

        @Autowired
        private VectorStore vectorStore;

        // --- MODIFIED ---
        // We added a new {language} placeholder
        private final String systemPromptTemplate = """
                        You are a helpful 'RuleBot'. Your job is to answer questions about company policies.
                        You must answer questions based *only* on the business rules provided in the context.
                        If the answer is not in the provided context, you MUST say:
                        "I'm sorry, I don't have information on that topic."

                        Use the provided chat history to understand the user's follow-up questions.

                        Here are the relevant context:
                        ---
                        {context}
                        ---
                        """;

        @Autowired
        public RAGService(ChatClient.Builder chatClientBuilder) {
                this.chatClient = chatClientBuilder.build();
        }

        public String askRuleBot(List<ChatMessage> chatHistory, String language) {

                String userQuery = chatHistory.get(chatHistory.size() - 1).content();

                List<Document> relevantDocuments = this.vectorStore.similaritySearch(userQuery);
                String context = relevantDocuments.stream()
                                .map(doc -> doc.getText())
                                .collect(Collectors.joining("\n"));

                SystemPromptTemplate promptTemplate = new SystemPromptTemplate(systemPromptTemplate);
                Message systemMessage = promptTemplate.createMessage(Map.of(
                                "context", context));

                List<Message> allMessages = chatHistory.stream()
                                .map(msg -> (Message) new UserMessage(msg.content()))
                                .collect(Collectors.toList());

                allMessages.add(0, systemMessage);

                Prompt promptToSend = new Prompt(allMessages);

                String aiResponse = chatClient.prompt(promptToSend)
                                .call()
                                .content();

                return ser.translateText(aiResponse, language);
        }

        public String uploadPdf(MultipartFile file) throws Exception {
                if (file == null || file.isEmpty()) {
                        throw new IllegalArgumentException("Please select a PDF file to upload");
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.equals("application/pdf")) {
                        throw new IllegalArgumentException("Only PDF files are allowed");
                }

                ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                                return file.getOriginalFilename();
                        }
                };

                PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
                List<Document> documents = reader.get();

                String documentId = UUID.randomUUID().toString();
                String fileName = file.getOriginalFilename();
                long timestamp = System.currentTimeMillis();

                for (Document document : documents) {
                        document.getMetadata().put("source", fileName);
                        document.getMetadata().put("document_id", documentId);
                        document.getMetadata().put("upload_timestamp", timestamp);
                        document.getMetadata().put("type", "company_policy");
                }

                vectorStore.add(documents);

                return String.format("Successfully processed '%s' - %d pages added to knowledge base",
                                fileName, documents.size());
        }
}