package com.vit.hackathon.rulebot.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private VectorStore vectorStore; // Our Neon PGVector database

    @Value("classpath:business-rules.txt")
    private Resource rulesFile; // Our knowledge base

    @Override
    public void run(String... args) throws Exception {

        // 1. Read the text from our file
        String allRules = rulesFile.getContentAsString(StandardCharsets.UTF_8);

        // 2. Create a single "Document" from this text
        Document document = new Document(allRules);

        // 3. Split the document into smaller chunks
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocuments = splitter.apply(List.of(document));

        // 4. Add (embed) these chunks into the vector database
        this.vectorStore.add(splitDocuments);

        System.out.println(">>> Successfully loaded business rules into the vector store.");
    }
}