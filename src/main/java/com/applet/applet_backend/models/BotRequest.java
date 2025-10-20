package com.applet.applet_backend.models;

public class BotRequest {
    private String message;
    private String personality;

    // Constructores
    public BotRequest() {}

    public BotRequest(String message, String personality) {
        this.message = message;
        this.personality = personality;
    }

    // Getters y Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    @Override
    public String toString() {
        return "BotRequest{" +
                "message='" + message + '\'' +
                ", personality='" + personality + '\'' +
                '}';
    }
}