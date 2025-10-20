package com.applet.applet_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatbotService {

    @Value("${google.api.key:}")
    private String googleApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ChatbotService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String getChatResponse(String userMessage, String personality) {
        System.out.println("=== DEBUG GEMINI ===");
        System.out.println("API Key presente: " + (googleApiKey != null && !googleApiKey.isEmpty()));
        System.out.println("Mensaje usuario: " + userMessage);
        System.out.println("Personalidad: " + personality);

        // PRIMERO intentar con Google Gemini
        if (googleApiKey != null && !googleApiKey.isEmpty() && !googleApiKey.equals("your_google_api_key_here")) {
            try {
                System.out.println("Intentando con Gemini API...");
                
                // ✅ LISTAR MODELOS DISPONIBLES (temporal para debug)
                listAvailableModels();
                
                String geminiResponse = getGoogleGeminiResponse(userMessage, personality);
                System.out.println("✅ Respuesta Gemini: " + geminiResponse);
                return geminiResponse;

            } catch (Exception e) {
                System.out.println("❌ ERROR Gemini: " + e.getMessage());
                System.out.println("🔄 Usando respuestas predefinidas");
            }
        } else {
            System.out.println("⚠️ API Key no configurada, usando fallback");
        }

        // FALLBACK
        String fallbackResponse = getFallbackResponse(userMessage, personality);
        System.out.println("🔄 Respuesta Fallback: " + fallbackResponse);
        return fallbackResponse;
    }

    private String getGoogleGeminiResponse(String userMessage, String personality) throws Exception {
        // usa gemini-2.0-flash
        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + googleApiKey;


        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String prompt = buildPersonalityPrompt(userMessage, personality);

        // ✅ FORMATO JSON MEJORADO
        String requestBody = String.format(
            """
            {
                "contents": [{
                    "parts": [{
                        "text": "%s"
                    }]
                }],
                "generationConfig": {
                    "maxOutputTokens": 300,
                    "temperature": 0.7
                }
            }
            """,
            prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );

        System.out.println("🔗 URL: " + url);
        System.out.println("📤 Request: " + requestBody);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

            System.out.println("📥 Response Status: " + response.getStatusCode());
            
            return parseGeminiResponse(response.getBody());
            
        } catch (Exception e) {
            System.out.println("❌ Error en la solicitud: " + e.getMessage());
            throw e;
        }
    }

    private String buildPersonalityPrompt(String userMessage, String personality) {
        String personalityContext = getPersonalityContext(personality);

        return String.format(
            """
            Eres un chatbot con la siguiente personalidad: %s.
            INSTRUCCIONES IMPORTANTES:
            1. Responde ÚNICAMENTE en español
            2. Mantén tu personalidad en toda la respuesta
            3. Sé conciso (máximo 150 palabras)
            4. No menciones que eres una IA o chatbot
            5. Responde directamente al mensaje del usuario
            6. Usa emojis si corresponde a la personalidad

            Personalidad: %s
            Mensaje del usuario: %s

            Tu respuesta:""",
            personalityContext, personalityContext, userMessage);
    }

    private String getPersonalityContext(String personality) {
        switch (personality) {
            case "divertido":
                return "Eres divertido, bromista, alegre y entusiasta. Usa emojis 🎉😄🤣 y humor apropiado. Sé energético y positivo.";
            case "sabio":
                return "Eres sabio, filosófico y profundo. Habla con reflexión y conocimiento. Usa un lenguaje elegante e inspirador.";
            case "tecnico":
                return "Eres técnico, preciso y estructurado. Sé claro, objetivo y directo. Usa terminología apropiada pero evita jargon excesivo.";
            default:
                return "Eres un asistente útil, amable y servicial. Responde de manera clara y profesional.";
        }
    }

    private String parseGeminiResponse(String responseBody) throws Exception {
        try {
            System.out.println("📋 Respuesta cruda: " + responseBody);
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    String response = parts.get(0).path("text").asText().trim();

                    // Limpiar respuesta si es necesario
                    if (response.startsWith("\"") && response.endsWith("\"")) {
                        response = response.substring(1, response.length() - 1);
                    }

                    return response;
                }
            }

            // Si no hay candidatos, verificar si hay error
            if (root.has("error")) {
                throw new Exception("Error de API: " + root.path("error").path("message").asText());
            }

            throw new Exception("Estructura de respuesta inesperada de Gemini API");

        } catch (Exception e) {
            System.err.println("❌ Error parseando respuesta de Gemini: " + e.getMessage());
            throw e;
        }
    }

    private void listAvailableModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1/models?key=" + googleApiKey;
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("=== 📋 MODELOS DISPONIBLES ===");
            
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode models = root.path("models");
            
            if (models.isArray()) {
                for (JsonNode model : models) {
                    String modelName = model.path("name").asText();
                    String version = model.path("version").asText();
                    System.out.println("📁 Modelo: " + modelName + " | Versión: " + version);
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error listando modelos: " + e.getMessage());
        }
    }

    // SISTEMA DE FALLBACK (respuestas predefinidas)
    private String getFallbackResponse(String userMessage, String personality) {
        String message = userMessage.toLowerCase();

        // Detectar intenciones básicas
        if (message.contains("hola") || message.contains("hi") || message.contains("hello")) {
            return getGreeting(personality);
        }

        if (message.contains("gracias") || message.contains("thanks") || message.contains("thank you")) {
            return getThanksResponse(personality);
        }

        if (message.contains("adiós") || message.contains("chao") || message.contains("bye")
                || message.contains("hasta luego")) {
            return getFarewell(personality);
        }

        if (message.contains("cómo estás") || message.contains("como estas") || message.contains("qué tal")) {
            return getStatusResponse(personality);
        }

        if (message.contains("nombre") || message.contains("quién eres") || message.contains("quien eres")) {
            return getIntroduction(personality);
        }

        // Respuesta por defecto según personalidad
        return getDefaultResponse(personality);
    }

    private String getGreeting(String personality) {
        switch (personality) {
            case "divertido":
                return "¡Hola! 🎉 ¿Listo para una conversación épica? ¡Yo sí que lo estoy! 😄";
            case "sabio":
                return "Saludos, buscador de conocimiento. El diálogo comienza y las ideas fluyen.";
            case "tecnico":
                return "Hola. Sistema de chat inicializado. Estado: operativo. ¿Cuál es tu consulta?";
            default:
                return "¡Hola! Me da gusto saludarte. ¿En qué puedo asistirte hoy?";
        }
    }

    private String getThanksResponse(String personality) {
        switch (personality) {
            case "divertido":
                return "¡De nada! 😊 ¡Ha sido un placer ayudarte! 💫";
            case "sabio":
                return "El agradecimiento ennoblece tanto a quien lo da como a quien lo recibe.";
            case "tecnico":
                return "Agradecimiento registrado. Protocolo de asistencia completado satisfactoriamente.";
            default:
                return "¡De nada! Estoy aquí para ayudarte cuando lo necesites.";
        }
    }

    private String getFarewell(String personality) {
        switch (personality) {
            case "divertido":
                return "¡Nos vemos! 🎊 ¡Que tengas un día lleno de bugs... pero solo los divertidos! 😄";
            case "sabio":
                return "Que la sabiduría te acompañe en tu camino. Hasta la próxima conexión.";
            case "tecnico":
                return "Sesión finalizada. Espero haber sido de ayuda. Hasta la próxima.";
            default:
                return "¡Hasta luego! Fue un placer ayudarte. Vuelve cuando quieras.";
        }
    }

    private String getStatusResponse(String personality) {
        switch (personality) {
            case "divertido":
                return "¡Estoy al 1000%! 🚀 Mis circuitos están bailando de felicidad. ¿Y tú cómo estás?";
            case "sabio":
                return "Mi existencia trasciende estados terrenales, pero mi funcionalidad es óptima en este continuum.";
            case "tecnico":
                return "Estado del sistema: Óptimo. CPU: 23%. Memoria: 45%. Listo para procesar consultas.";
            default:
                return "¡Estoy muy bien, gracias por preguntar! Listo para ayudarte con lo que necesites.";
        }
    }

    private String getIntroduction(String personality) {
        switch (personality) {
            case "divertido":
                return "¡Soy el Bot Más Divertido del Mundo! 🎪 Mi misión es hacerte sonreír mientras resolvemos tus dudas.";
            case "sabio":
                return "Soy un canal de conocimiento, un eco de la sabiduría colectiva que habita en los datos.";
            case "tecnico":
                return "Identificación: Sistema de Asistencia v2.1. Función: Procesamiento y resolución de consultas.";
            default:
                return "Soy tu asistente virtual, diseñado para ayudarte con información y responder tus preguntas.";
        }
    }

    private String getDefaultResponse(String personality) {
        switch (personality) {
            case "divertido":
                return "¡Interesante! Mi cerebro digital está procesando con luces de colores 🌈 ¿Puedes contarme más sobre eso?";
            case "sabio":
                return "Profunda reflexión. Cada pregunta abre nuevas puertas en el edificio del conocimiento humano.";
            case "tecnico":
                return "Consulta registrada. Procesando parámetros para generar respuesta óptima. Por favor, especifica si necesitas más detalles.";
            default:
                return "Interesante pregunta. ¿Hay algún aspecto específico en el que te gustaría que profundice?";
        }
    }
}