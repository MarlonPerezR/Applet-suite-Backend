package com.applet.applet_backend.Controllers;

import com.applet.applet_backend.models.BotRequest;
import com.applet.applet_backend.models.BotResponse;
import com.applet.applet_backend.service.ChatbotService;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:3000") // Ajusta el origen según sea necesario
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<BotResponse> chat(@RequestBody BotRequest request) {
        try {
            String response = chatbotService.getChatResponse(
                request.getMessage(), 
                request.getPersonality()
            );
            
            BotResponse botResponse = new BotResponse(response, request.getPersonality());
            return ResponseEntity.ok(botResponse);
            
} catch (Exception e) {
    BotResponse errorResponse = new BotResponse(
        "Lo siento, estoy teniendo problemas para responder. ¿Podrías intentarlo de nuevo?", 
        request.getPersonality()
    );
    return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(errorResponse);
}
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running!");
    }
}