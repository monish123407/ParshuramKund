package com.parshuramKund.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import com.parshuramKund.DTO.InquiryDTO;
import com.parshuramKund.Service.InquiryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
@Slf4j
public class InquiryController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InquiryController.class);

    @Autowired
    private InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<InquiryDTO> createInquiry(@RequestBody InquiryDTO request) {
        log.info("Saving new inquiry from: {}", request.getName());
        try {
            InquiryDTO response = inquiryService.saveInquiry(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error saving inquiry", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public List<InquiryDTO> getAllInquiries() {
        log.info("Fetching all inquiries");
        return inquiryService.getAllInquiries();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInquiry(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Role", required = false) String role) {
        if ("AUDITOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "AUDITOR cannot delete inquiries"));
        }
        log.info("Disposing inquiry ID: {}", id);
        try {
            inquiryService.deleteInquiry(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting inquiry ID {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/dispose")
    public ResponseEntity<?> disposeInquiry(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @RequestHeader(value = "X-Admin-Role", required = false) String role) {
        if ("AUDITOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "AUDITOR cannot dispose inquiries"));
        }
        String message = body.get("message");
        log.info("Disposing inquiry ID: {} with admin message", id);
        try {
            inquiryService.disposeInquiryWithMessage(id, message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error disposing inquiry ID {} with message", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
