package com.applet.applet_backend.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BotResponse {
    private String response;
    private String personality;
    private String timestamp;

    // Constructores
    public BotResponse() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public BotResponse(String response, String personality) {
        this.response = response;
        this.personality = personality;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Getters y Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BotResponse{" +
                "response='" + response + '\'' +
                ", personality='" + personality + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}