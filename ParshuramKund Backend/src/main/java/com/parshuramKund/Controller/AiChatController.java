package com.parshuramKund.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.parshuramKund.DTO.ChatMessageDTO;
import com.parshuramKund.Service.AiChatService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
@Slf4j
public class AiChatController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiChatController.class);

    @Autowired
    private AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatMessageDTO> chat(@RequestBody ChatMessageDTO request) {
        log.info("Received AI chat query: {}", request.getMessage());
        try {
            String botResponse = aiChatService.generateResponse(request.getMessage());
            ChatMessageDTO response = new ChatMessageDTO(request.getMessage(), botResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating chatbot response", e);
            ChatMessageDTO errorResponse = new ChatMessageDTO(request.getMessage(), 
                "I apologize, I am experiencing temporary difficulties. Please try again shortly.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
