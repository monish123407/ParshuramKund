package com.parshuramKund.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.parshuramKund.DTO.AdminUserDTO;
import com.parshuramKund.Service.AdminUserService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        AdminUserDTO response = adminUserService.login(username, password);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }
    }

    @GetMapping("/members")
    public ResponseEntity<?> getAllMembers(@RequestHeader(value = "X-Admin-Role", required = false) String role) {
        if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only SUPER_ADMIN can manage members"));
        }
        List<AdminUserDTO> members = adminUserService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @PostMapping("/members")
    public ResponseEntity<?> addMember(
            @RequestHeader(value = "X-Admin-Role", required = false) String role,
            @RequestBody AdminUserDTO request) {
        if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only SUPER_ADMIN can manage members"));
        }
        try {
            AdminUserDTO response = adminUserService.saveMember(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<?> updateMember(
            @RequestHeader(value = "X-Admin-Role", required = false) String role,
            @PathVariable Long id,
            @RequestBody AdminUserDTO request) {
        if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only SUPER_ADMIN can manage members"));
        }
        try {
            AdminUserDTO response = adminUserService.updateMember(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<?> deleteMember(
            @RequestHeader(value = "X-Admin-Role", required = false) String role,
            @PathVariable Long id) {
        if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only SUPER_ADMIN can manage members"));
        }
        try {
            adminUserService.deleteMember(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
