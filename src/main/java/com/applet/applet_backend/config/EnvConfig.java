package com.applet.applet_backend.config;

import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class EnvConfig {

    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv;

            // 🔹 Primero intenta cargar desde el entorno local
            File localEnv = new File(".env");

            if (localEnv.exists()) {
                dotenv = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
                System.out.println("✅ Cargando .env desde el entorno local");
            } else {
                // 🔹 Si no existe, intenta cargar desde la ruta de Render
                dotenv = Dotenv.configure()
                        .directory("/etc/secrets")
                        .filename(".env")
                        .ignoreIfMissing()
                        .load();
                System.out.println("✅ Cargando .env desde /etc/secrets (Render)");
            }

            // 🔸 Carga y asigna tus claves al sistema
            String youtubeApiKey = dotenv.get("YOUTUBE_API_KEY");
            String googleApiKey = dotenv.get("GOOGLE_API_KEY");
            String omdbApiKey = dotenv.get("OMDB_API_KEY");
            String weatherApiKey = dotenv.get("WEATHERAPI_KEY");

            if (youtubeApiKey != null) System.setProperty("YOUTUBE_API_KEY", youtubeApiKey.trim());
            if (googleApiKey != null) System.setProperty("GOOGLE_API_KEY", googleApiKey.trim());
            if (omdbApiKey != null) System.setProperty("OMDB_API_KEY", omdbApiKey.trim());
            if (weatherApiKey != null) System.setProperty("WEATHERAPI_KEY", weatherApiKey.trim());

            System.out.println("✅ Variables de entorno cargadas correctamente.");

        } catch (Exception e) {
            System.err.println("❌ Error cargando .env: " + e.getMessage());
        }
    }
}
