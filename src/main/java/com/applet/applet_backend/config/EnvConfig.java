package com.applet.applet_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

@Configuration
public class EnvConfig {
    
    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv = Dotenv.configure().load();
            String youtubeApiKey = dotenv.get("YOUTUBE_API_KEY");
            
            if (youtubeApiKey != null && !youtubeApiKey.trim().isEmpty()) {
                System.setProperty("YOUTUBE_API_KEY", youtubeApiKey.trim());
                System.out.println("✅ YouTube API Key cargada desde .env");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error cargando .env: " + e.getMessage());
        }
    }

    @Bean
    public String openaiApiKey() {
        return System.getenv("OPENAI_API_KEY");
    }
}