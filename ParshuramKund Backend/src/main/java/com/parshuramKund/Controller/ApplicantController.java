package com.parshuramKund.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;


import com.parshuramKund.DTO.ApplicantDTO;
import com.parshuramKund.Service.ApplicantService;
import com.parshuramKund.Service.PdfService;
import com.parshuramKund.Service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
@Slf4j
public class ApplicantController {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApplicantController.class);
	
	  @Autowired
	  private  ApplicantService applicantService ;
	  
	
	  
	  @Autowired
	    private PdfService pdfService;
	  @Autowired
	    private EmailService emailService;

	    @PostMapping("/register")
	    public ResponseEntity<?> register(
	             @RequestBody ApplicantDTO request
	    ) {
	    	log.info("Registering user: {}", request.getFullName());
	        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
	            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Email is required"));
	        }
	        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	        if (!request.getEmail().trim().matches(emailRegex)) {
	            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Invalid email format"));
	        }
	        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
	            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Mobile number is required"));
	        }
	        if (!request.getPhone().trim().matches("^[0-9]{10}$")) {
	            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Mobile number must be a valid 10-digit number"));
	        }
	        try {

	        	ApplicantDTO response = applicantService.registerUser(request);

	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            log.error("Error during registration", e);
	            return ResponseEntity.internalServerError().build();
	        }

	        
	    }

	    @GetMapping("/generate-pdf/{id}")
	    public ResponseEntity<?> generatePdf(
	            @PathVariable String id,
	            @RequestHeader(value = "X-Admin-Role", required = false) String role,
	            @RequestParam(value = "role", required = false) String roleQuery) {
	        String finalRole = role != null ? role : roleQuery;
	        if ("AUDITOR".equalsIgnoreCase(finalRole)) {
	            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
	                    .body(java.util.Map.of("error", "AUDITOR cannot download passes"));
	        }
	        try {
	            ApplicantDTO applicant = applicantService.findById(id);
	            if (applicant != null && Boolean.TRUE.equals(applicant.getRejected())) {
	                return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
	                        .body(java.util.Map.of("error", "Rejected registration cannot generate pass"));
	            }
	            byte[] pdf = pdfService.generatePdf(id);
	            return ResponseEntity.ok()
	                    .header(HttpHeaders.CONTENT_DISPOSITION,
	                            "inline; filename=registration-pass.pdf")
	                    .contentType(MediaType.APPLICATION_PDF)
	                    .body(pdf);
	        } catch (Exception e) {
	            log.error("Error generating PDF for ID {}: ", id, e);
	            return ResponseEntity.internalServerError().build();
	        }
	    }
	    @GetMapping("/mobile/{mobileNo}")
	    public List<ApplicantDTO> getByMobile(
	            @PathVariable String mobileNo) {

	        return applicantService.findByPhone(mobileNo);

	    }

	    @GetMapping("/registrations")
	    public List<ApplicantDTO> getAllRegistrations() {
	        return applicantService.findAllRegistrations();
	    }

	    @DeleteMapping("/registrations/{id}")
	    public ResponseEntity<?> deleteRegistration(
	            @PathVariable String id,
	            @RequestHeader(value = "X-Admin-Role", required = false) String role) {
	        if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
	            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
	                    .body(java.util.Map.of("error", "Only SUPER_ADMIN can cancel registrations"));
	        }
	        try {
	            applicantService.deleteRegistration(id);
	            return ResponseEntity.ok().build();
	        } catch (Exception e) {
	            log.error("Error deleting registration for ID {}: ", id, e);
	            return ResponseEntity.internalServerError().build();
	        }
	    }

	    @PostMapping("/registrations/{id}/resend-email")
	    public ResponseEntity<?> resendEmail(
	            @PathVariable String id,
	            @RequestHeader(value = "X-Admin-Role", required = false) String role) {
	        if ("AUDITOR".equalsIgnoreCase(role)) {
	            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
	                    .body(java.util.Map.of("error", "AUDITOR cannot resend pass emails"));
	        }
	        try {
	            ApplicantDTO applicant = applicantService.findById(id);
	            if (applicant == null) {
	                return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
	                        .body(java.util.Map.of("error", "Registration not found"));
	            }
	            if (Boolean.TRUE.equals(applicant.getRejected())) {
	                return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
	                        .body(java.util.Map.of("error", "Rejected registration cannot resend email"));
	            }
	            emailService.sendRegistrationEmail(applicant);
	            return ResponseEntity.ok(java.util.Map.of("message", "Email queued for dispatch successfully"));
	        } catch (Exception e) {
	            log.error("Error resending email for ID {}: ", id, e);
	            return ResponseEntity.internalServerError().build();
	        }
	    }

	    @PostMapping("/registrations/{id}/verify")
	    public ResponseEntity<?> verifyRegistration(
	            @PathVariable String id,
	            @RequestHeader(value = "X-Admin-Role", required = false) String role,
	            @RequestHeader(value = "X-Admin-Username", required = false) String username) {
	        if ("AUDITOR".equalsIgnoreCase(role)) {
	            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
	                    .body(java.util.Map.of("error", "AUDITOR role cannot verify pilgrim passes"));
	        }
	        try {
	            ApplicantDTO updated = applicantService.verifyRegistration(id, username);
	            return ResponseEntity.ok(updated);
	        } catch (Exception e) {
	            log.error("Error verifying registration for ID {}: ", id, e);
	            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
	                    .body(java.util.Map.of("error", e.getMessage()));
	        }
	    }

	    @PostMapping("/registrations/{id}/reject")
	    public ResponseEntity<?> rejectRegistration(
	            @PathVariable String id,
	            @RequestHeader(value = "X-Admin-Role", required = false) String role,
	            @RequestHeader(value = "X-Admin-Username", required = false) String username) {
	        if ("AUDITOR".equalsIgnoreCase(role)) {
	            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
	                    .body(java.util.Map.of("error", "AUDITOR role cannot reject pilgrim passes"));
	        }
	        try {
	            ApplicantDTO updated = applicantService.rejectRegistration(id, username);
	            return ResponseEntity.ok(updated);
	        } catch (Exception e) {
	            log.error("Error rejecting registration for ID {}: ", id, e);
	            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
	                    .body(java.util.Map.of("error", e.getMessage()));
	        }
	    }

    private final java.util.Map<String, OtpDetails> otpCache = new java.util.concurrent.ConcurrentHashMap<>();

    private static class OtpDetails {
        String otp;
        java.time.LocalDateTime expiryTime;

        OtpDetails(String otp, java.time.LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    @GetMapping("/search")
    public List<ApplicantDTO> search(@RequestParam("query") String query) {
        if (query != null && query.contains("@") && query.contains(".")) {
            // Block direct email searches to enforce OTP verification
            log.warn("Blocked direct email search attempt for query: {}", query);
            return new java.util.ArrayList<>();
        }
        return applicantService.search(query);
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@RequestParam("email") String email) {
        log.info("Requesting OTP for email: {}", email);
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Please provide a valid email address"));
        }

        try {
            // Internally query the service to check if registrations exist
            List<ApplicantDTO> registrations = applicantService.search(email.trim());
            if (registrations.isEmpty()) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "No registration found for this email address"));
            }

            // Generate 6-digit OTP
            String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
            otpCache.put(email.toLowerCase().trim(), new OtpDetails(otp, java.time.LocalDateTime.now().plusMinutes(5)));

            // Dispatch OTP email
            emailService.sendOtpEmail(email.trim(), otp);
            log.info("OTP generated and sent to: {}. OTP: {}", email, otp);

            return ResponseEntity.ok(java.util.Map.of("message", "OTP sent successfully to your email"));
        } catch (Exception e) {
            log.error("Error sending OTP for email: {}", email, e);
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Failed to send OTP"));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestParam("email") String email, @RequestParam("otp") String otp) {
        log.info("Verifying OTP for email: {}", email);
        if (email == null || email.trim().isEmpty() || otp == null || otp.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Email and OTP are required"));
        }

        String key = email.toLowerCase().trim();
        OtpDetails details = otpCache.get(key);

        if (details == null || !details.otp.equals(otp.trim())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", "Invalid OTP. Please try again."));
        }

        if (details.expiryTime.isBefore(java.time.LocalDateTime.now())) {
            otpCache.remove(key);
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", "OTP has expired. Please request a new one."));
        }

        // OTP is valid! Clear it to prevent reuse
        otpCache.remove(key);

        try {
            // Retrieve registrations for this email and return them
            List<ApplicantDTO> registrations = applicantService.search(email.trim());
            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            log.error("Error retrieving registrations for verified email: {}", email, e);
            return ResponseEntity.internalServerError().build();
        }
    }


	    @PostMapping("/upload-aadhar")
	    public ResponseEntity<java.util.Map<String, String>> uploadAadharPhoto(
	            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
	        try {
	            if (file.isEmpty()) {
	                return ResponseEntity.badRequest().body(java.util.Map.of("error", "File is empty"));
	            }
	            
	            // Check file size (200KB maximum)
	            if (file.getSize() > 200L * 1024) {
	                return ResponseEntity.badRequest().body(java.util.Map.of("error", "File size exceeds 200KB limit"));
	            }

	            
	            // Check content type (must be image or pdf)
	            String contentType = file.getContentType();
	            if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
	                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Only images and PDF files are allowed"));
	            }
	            
	            // Resolve upload directory
	            java.nio.file.Path uploadDir = java.nio.file.Paths.get("aadhar-photos").toAbsolutePath().normalize();
	            java.io.File dir = uploadDir.toFile();
	            if (!dir.exists()) {
	                dir.mkdirs();
	            }
	            
	            // Generate unique filename to prevent collision and mask user identity
	            String originalFileName = file.getOriginalFilename();
	            String extension = "";
	            if (originalFileName != null && originalFileName.contains(".")) {
	                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
	            }
	            String randomFileName = java.util.UUID.randomUUID().toString() + extension;
	            java.nio.file.Path filePath = uploadDir.resolve(randomFileName);
	            
	            // Save file
	            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	            
	            // Return filename/path
	            return ResponseEntity.ok(java.util.Map.of("filePath", randomFileName));
	        } catch (Exception e) {
	            log.error("Error uploading Aadhaar photo", e);
	            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Failed to upload file"));
	        }
	    }

	    @GetMapping("/aadhar-photo/{id}")
	    public ResponseEntity<org.springframework.core.io.Resource> getAadharPhoto(@PathVariable String id) {
	        try {
	            ApplicantDTO applicant = applicantService.findById(id);
	            if (applicant == null || applicant.getAadharPhotoPath() == null || applicant.getAadharPhotoPath().isEmpty()) {
	                return ResponseEntity.notFound().build();
	            }
	            
	            java.nio.file.Path filePath = java.nio.file.Paths.get("aadhar-photos")
	                    .resolve(applicant.getAadharPhotoPath()).toAbsolutePath().normalize();
	            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
	            
	            if (!resource.exists() || !resource.isReadable()) {
	                return ResponseEntity.notFound().build();
	            }
	            
	            // Detect content type
	            String contentType = "application/octet-stream";
	            try {
	                contentType = java.nio.file.Files.probeContentType(filePath);
	                if (contentType == null) {
	                    if (filePath.toString().endsWith(".pdf")) {
	                        contentType = "application/pdf";
	                    } else {
	                        contentType = "image/jpeg"; // default fallback for image
	                    }
	                }
	            } catch (Exception ex) {
	                log.warn("Could not determine file type.");
	            }
	            
	            return ResponseEntity.ok()
	                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
	                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
	                    .body(resource);
	        } catch (Exception e) {
	            log.error("Error retrieving Aadhaar photo for ID {}: ", id, e);
	            return ResponseEntity.internalServerError().build();
	        }
	    }

	    @PostMapping("/delete-aadhar")
	    public ResponseEntity<?> deleteAadharPhoto(@RequestBody java.util.Map<String, String> request) {
	        try {
	            String fileName = request.get("filePath");
	            if (fileName == null || fileName.trim().isEmpty() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
	                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Invalid file name"));
	            }
	            
	            java.nio.file.Path uploadDir = java.nio.file.Paths.get("aadhar-photos").toAbsolutePath().normalize();
	            java.nio.file.Path filePath = uploadDir.resolve(fileName).normalize();
	            
	            if (!filePath.startsWith(uploadDir)) {
	                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Unauthorized path"));
	            }
	            
	            java.io.File file = filePath.toFile();
	            if (file.exists() && file.isFile()) {
	                if (file.delete()) {
	                    log.info("Deleted abandoned Aadhaar file: {}", fileName);
	                    return ResponseEntity.ok(java.util.Map.of("message", "File deleted successfully"));
	                } else {
	                    log.error("Failed to delete Aadhaar file: {}", fileName);
	                    return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Failed to delete file"));
	                }
	            }
	            return ResponseEntity.notFound().build();
	        } catch (Exception e) {
	            log.error("Error deleting Aadhaar photo", e);
	            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Error occurred during deletion"));
	        }
	    }
}

