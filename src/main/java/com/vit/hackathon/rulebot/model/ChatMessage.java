package com.vit.hackathon.rulebot.model;

// This is a simple data-holder for one message.
// "role" will be "user" or "bot"
// "content" will be the text of the message
public record ChatMessage(String role, String content) {
}