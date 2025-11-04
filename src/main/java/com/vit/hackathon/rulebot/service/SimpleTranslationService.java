package com.vit.hackathon.rulebot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class SimpleTranslationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String translateText(String text, String targetLanguage) {
        try {
            String languageCode = getLanguageCode(targetLanguage);
            if (languageCode.equals("en")) {
                return text;
            }

            // Using a simple translation API (MyMemory - free, no key required)
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.mymemory.translated.net/get?q=%s&langpair=en|%s",
                    encodedText, languageCode);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            Map<String, Object> responseData = (Map<String, Object>) response.get("responseData");

            return (String) responseData.get("translatedText");

        } catch (Exception e) {
            System.err.println("Translation error: " + e.getMessage());
            return text;
        }
    }

    private String getLanguageCode(String language) {
        return switch (language.toLowerCase()) {
            case "hindi" -> "hi";
            case "telugu" -> "te";
            case "tamil" -> "ta";
            case "spanish" -> "es";
            case "french" -> "fr";
            case "german" -> "de";
            default -> "en";
        };
    }
}
