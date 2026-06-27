package com.parshuramKund.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.parshuramKund.DTO.ApplicantDTO;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

@Service
@Slf4j
public class EmailServiceimpl implements EmailService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailServiceimpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private PdfService pdfService;

    @Override
    @Async
    public void sendRegistrationEmail(ApplicantDTO applicant) {
        if (applicant.getEmail() == null || applicant.getEmail().trim().isEmpty()) {
            log.info("No email address provided for Applicant ID: {}, skipping email dispatch.", applicant.getId());
            return;
        }

        try {
            if (mailSender == null) {
                log.warn("JavaMailSender is not initialized. Printing registration email content to log.");
                printEmailToConsole(applicant);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(applicant.getEmail());
            helper.setSubject("Parshuram Kund Mela 2027 - Registration Pass Details");
            
            String htmlContent = buildEmailContent(applicant);
            helper.setText(htmlContent, true);

            try {
                byte[] pdfBytes = pdfService.generatePdf(applicant.getId());
                if (pdfBytes != null) {
                    helper.addAttachment("RegistrationPass_" + applicant.getId() + ".pdf", 
                        new org.springframework.core.io.ByteArrayResource(pdfBytes), "application/pdf");
                    log.info("Attached PDF pass for registration ID: {}", applicant.getId());
                }
            } catch (Exception e) {
                log.error("Could not generate or attach PDF pass for registration ID: {}. Error: {}", 
                    applicant.getId(), e.getMessage());
            }

            mailSender.send(message);
            log.info("Successfully sent registration email to: {}", applicant.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration email to: {}. Error: {}", applicant.getEmail(), e.getMessage());
            log.info("Fallback: Printing registration email details below:");
            printEmailToConsole(applicant);
        }
    }

    private String buildEmailContent(ApplicantDTO applicant) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.5;'>");
        sb.append("<div style='background-color: #1a2a40; padding: 20px; text-align: center; color: white;'>");
        sb.append("<h2 style='margin: 0;'>Parshuram Kund Makar Sankranti Mela 2027</h2>");
        sb.append("</div>");
        sb.append("<div style='padding: 20px; border: 1px solid #ddd; border-top: none; background-color: #ffffff;'>");
        sb.append("<p>Dear <strong>").append(applicant.getFullName()).append("</strong>,</p>");
        sb.append("<p>Your registration for the Parshuram Kund Mela 2027 has been successfully confirmed. Below are your pass details:</p>");
        
        sb.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px;'>");
        
        sb.append("<tr><td style='padding: 8px; border-bottom: 1px solid #eee; width: 40%; font-weight: bold; color: #1a2a40;'>Registration ID:</td>");
        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>#").append(applicant.getId()).append("</td></tr>");
        
        sb.append("<tr><td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; color: #1a2a40;'>Holy Dip Date:</td>");
        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(applicant.getHolyDipDate()).append("</td></tr>");
        
        sb.append("<tr><td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; color: #1a2a40;'>Booking Date:</td>");
        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(applicant.getBookingDate()).append("</td></tr>");
        
        sb.append("<tr><td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; color: #1a2a40;'>Age / Gender:</td>");
        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(applicant.getAge()).append(" / ").append(applicant.getGender()).append("</td></tr>");
        
        sb.append("<tr><td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; color: #1a2a40;'>Phone:</td>");
        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(applicant.getPhone()).append("</td></tr>");
        
        if (applicant.getAadharNumber() != null && !applicant.getAadharNumber().trim().isEmpty()) {
            sb.append("<tr><td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; color: #1a2a40;'>Aadhaar Number:</td>");
            sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(applicant.getAadharNumber()).append("</td></tr>");
        }

        
        sb.append("<tr><td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; color: #1a2a40;'>Comorbidities:</td>");
        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(applicant.getComorbidities()).append("</td></tr>");
        
        sb.append("<tr><td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; color: #1a2a40;'>Present Address:</td>");
        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(applicant.getPresentAddress()).append(", ").append(applicant.getPresentDistrict()).append(", ").append(applicant.getPresentState()).append(" - ").append(applicant.getPresentPinCode()).append("</td></tr>");
        
        sb.append("</table>");
        
        // Co-applicants list parsing
        if (applicant.getCoApplicant() != null && !applicant.getCoApplicant().isEmpty() && !"[]".equals(applicant.getCoApplicant())) {
            sb.append("<h4 style='color: #8c7647; margin-top: 20px; margin-bottom: 8px; border-bottom: 1px dashed #dcd7ca; padding-bottom: 4px;'>Co-Applicants Registered:</h4>");
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<java.util.Map<String, Object>> list = objectMapper.readValue(
                    applicant.getCoApplicant(), 
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
                );
                
                if (list != null && !list.isEmpty()) {
                    sb.append("<table style='width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 13px;'>");
                    sb.append("<thead><tr style='background-color: #faf9f6; border-bottom: 2px solid #e9e5d9;'>");
                    sb.append("<th style='padding: 8px; text-align: left; color: #1a2a40; font-weight: bold;'>#</th>");
                    sb.append("<th style='padding: 8px; text-align: left; color: #1a2a40; font-weight: bold;'>Name</th>");
                    sb.append("<th style='padding: 8px; text-align: left; color: #1a2a40; font-weight: bold;'>Age</th>");
                    sb.append("<th style='padding: 8px; text-align: left; color: #1a2a40; font-weight: bold;'>Gender</th>");
                    sb.append("<th style='padding: 8px; text-align: left; color: #1a2a40; font-weight: bold;'>Comorbidities</th>");
                    sb.append("</tr></thead><tbody>");
                    
                    int index = 1;
                    for (java.util.Map<String, Object> co : list) {
                        String comorbiditiesVal = (String) co.get("comorbidities");
                        if (comorbiditiesVal == null || comorbiditiesVal.trim().isEmpty()) {
                            comorbiditiesVal = "None";
                        }
                        sb.append("<tr>");
                        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee; color: #777;'>").append(index++).append("</td>");
                        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold;'>").append(co.getOrDefault("name", "")).append("</td>");
                        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(co.getOrDefault("age", "")).append("</td>");
                        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(co.getOrDefault("gender", "")).append("</td>");
                        sb.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'><span style='background: #faf9f6; padding: 2px 6px; border: 1px solid #e9e5d9; border-radius: 4px; font-size: 11px;'>").append(comorbiditiesVal).append("</span></td>");
                        sb.append("</tr>");
                    }
                    sb.append("</tbody></table>");
                } else {
                    sb.append("<p style='font-size: 13px; color: #555; background: #faf9f6; padding: 10px; border: 1px solid #e9e5d9; border-radius: 6px;'>None</p>");
                }
            } catch (Exception e) {
                log.error("Failed to parse co-applicants JSON in email builder. Falling back to raw string.", e);
                sb.append("<p style='font-size: 13px; color: #555; background: #faf9f6; padding: 10px; border: 1px solid #e9e5d9; border-radius: 6px;'>").append(applicant.getCoApplicant()).append("</p>");
            }
        }
        
        sb.append("<p style='margin-top: 30px; font-size: 13px; color: #777;'>Please present this email or download your registration pass PDF from the official website to enter the Mela premises.</p>");
        sb.append("<p style='font-weight: bold; color: #8c7647; margin-top: 15px;'>Wishing you a sacred and safe dip journey!</p>");
        sb.append("</div>");
        sb.append("<div style='background-color: #faf9f6; padding: 15px; font-size: 11px; text-align: center; color: #777; border: 1px solid #ddd; border-top: none;'>");
        sb.append("This is an automatically generated email. Please do not reply directly.");
        sb.append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private void printEmailToConsole(ApplicantDTO applicant) {
        log.info("\n=============================================================");
        log.info("REGISTRATION EMAIL NOTIFICATION SIMULATOR");
        log.info("To: {}", applicant.getEmail());
        log.info("Subject: Parshuram Kund Mela 2027 - Registration Pass Details");
        log.info("Attachment: RegistrationPass_{}.pdf (PDF bytes generated and attached)", applicant.getId());
        log.info("Body Contents:\n{}", buildEmailContent(applicant));
        log.info("=============================================================\n");
    }

    @Override
    @Async
    public void sendInquiryReplyEmail(String recipientEmail, String userName, String inquirySubject, String inquiryMessage, String replyMessage) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            log.info("No email address provided for Inquiry reply, skipping email dispatch.");
            return;
        }

        try {
            if (mailSender == null) {
                log.warn("JavaMailSender is not initialized. Printing inquiry reply email content to log.");
                printInquiryReplyToConsole(recipientEmail, userName, inquirySubject, inquiryMessage, replyMessage);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            String subjectHeader = "Inquiry Response: " + (inquirySubject != null && !inquirySubject.trim().isEmpty() ? inquirySubject : "Regarding Parshuram Kund Mela");
            helper.setSubject(subjectHeader);
            
            String htmlContent = buildInquiryReplyContent(userName, inquirySubject, inquiryMessage, replyMessage);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully sent inquiry reply email to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send inquiry reply email to: {}. Error: {}", recipientEmail, e.getMessage());
            log.info("Fallback: Printing inquiry reply email details below:");
            printInquiryReplyToConsole(recipientEmail, userName, inquirySubject, inquiryMessage, replyMessage);
        }
    }

    private String buildInquiryReplyContent(String userName, String inquirySubject, String inquiryMessage, String replyMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.5;'>");
        sb.append("<div style='background-color: #1a2a40; padding: 20px; text-align: center; color: white;'>");
        sb.append("<h2 style='margin: 0;'>Parshuram Kund Makar Sankranti Mela 2027</h2>");
        sb.append("<p style='margin: 5px 0 0 0; font-size: 14px; color: #8c7647;'>Inquiry Response & Resolution</p>");
        sb.append("</div>");
        sb.append("<div style='padding: 20px; border: 1px solid #ddd; border-top: none; background-color: #ffffff;'>");
        sb.append("<p>Dear <strong>").append(userName).append("</strong>,</p>");
        sb.append("<p>Thank you for contacting the Mela administration. We have reviewed your inquiry and hope the following resolution/response is helpful to you:</p>");
        
        sb.append("<div style='background-color: #fcfbf9; border-left: 4px solid #8c7647; padding: 15px; margin: 20px 0; border-radius: 4px;'>");
        sb.append("<h4 style='margin: 0 0 10px 0; color: #1a2a40;'>Response from Mela Administrator:</h4>");
        sb.append("<p style='margin: 0; white-space: pre-wrap; font-size: 15px; color: #2d3748;'>").append(replyMessage).append("</p>");
        sb.append("</div>");
        
        sb.append("<div style='margin-top: 25px; padding-top: 15px; border-top: 1px dashed #eee;'>");
        sb.append("<h5 style='margin: 0 0 8px 0; color: #718096; text-transform: uppercase; font-size: 11px; letter-spacing: 0.5px;'>Your Original Inquiry:</h5>");
        sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 13px; color: #4a5568;'>");
        sb.append("<tr><td style='padding: 4px 0; font-weight: bold; width: 20%;'>Subject:</td>");
        sb.append("<td style='padding: 4px 0;'>").append(inquirySubject != null && !inquirySubject.trim().isEmpty() ? inquirySubject : "(No Subject)").append("</td></tr>");
        sb.append("<tr><td style='padding: 4px 0; font-weight: bold; vertical-align: top;'>Message:</td>");
        sb.append("<td style='padding: 4px 0; white-space: pre-wrap;'>").append(inquiryMessage).append("</td></tr>");
        sb.append("</table>");
        sb.append("</div>");
        
        sb.append("<p style='margin-top: 30px; font-size: 13px; color: #777;'>");
        sb.append("If you require additional assistance, please feel free to submit a new inquiry on our portal or contact the official Mela Helpline at <strong>+91 9233495795</strong>.");
        sb.append("</p>");
        sb.append("<p style='font-weight: bold; color: #8c7647; margin-top: 15px;'>Warm Regards,<br>Mela Control Room & Administration</p>");
        sb.append("</div>");
        sb.append("<div style='background-color: #faf9f6; padding: 15px; font-size: 11px; text-align: center; color: #777; border: 1px solid #ddd; border-top: none;'>");
        sb.append("This is an automatically generated email. Please do not reply directly.");
        sb.append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private void printInquiryReplyToConsole(String recipientEmail, String userName, String inquirySubject, String inquiryMessage, String replyMessage) {
        log.info("\n=============================================================");
        log.info("INQUIRY REPLY EMAIL NOTIFICATION SIMULATOR");
        log.info("To: {}", recipientEmail);
        log.info("Subject: Inquiry Response: {}", inquirySubject);
        log.info("Body Contents:\n{}", buildInquiryReplyContent(userName, inquirySubject, inquiryMessage, replyMessage));
        log.info("=============================================================\n");
    }

    @Override
    @Async
    public void sendOtpEmail(String recipientEmail, String otp) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            return;
        }

        try {
            if (mailSender == null) {
                log.warn("JavaMailSender is not initialized. Printing OTP email content to log. OTP for {} is {}", recipientEmail, otp);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject("Parshuram Kund Mela 2027 - OTP for Accessing Registration Pass");
            
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.5;'>");
            sb.append("<div style='background-color: #1a2a40; padding: 20px; text-align: center; color: white;'>");
            sb.append("<h2 style='margin: 0;'>Parshuram Kund Makar Sankranti Mela 2027</h2>");
            sb.append("</div>");
            sb.append("<div style='padding: 20px; border: 1px solid #ddd; border-top: none; background-color: #ffffff;'>");
            sb.append("<p>Dear Devotee,</p>");
            sb.append("<p>You have requested to access or print your registration pass(es) using your email ID.</p>");
            sb.append("<p>Please use the following One-Time Password (OTP) to complete your verification:</p>");
            sb.append("<div style='margin: 20px 0; padding: 15px; background-color: #f7f9fc; border: 1px dashed #b8860b; text-align: center; font-size: 28px; font-weight: bold; letter-spacing: 5px; color: #b8860b;'>");
            sb.append(otp);
            sb.append("</div>");
            sb.append("<p style='font-size: 13px; color: #777;'>This OTP is valid for 5 minutes. If you did not request this, please ignore this email.</p>");
            sb.append("</div>");
            sb.append("<div style='background-color: #f8fafc; padding: 15px; text-align: center; font-size: 12px; color: #777; border: 1px solid #ddd; border-top: none;'>");
            sb.append("District Administration, Lohit, Arunachal Pradesh");
            sb.append("</div>");
            sb.append("</body></html>");

            helper.setText(sb.toString(), true);
            mailSender.send(message);
            log.info("Successfully sent OTP email to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}. Error: {}", recipientEmail, e.getMessage());
            log.warn("Fallback: Printing OTP details below:");
            log.warn("OTP for {} is {}", recipientEmail, otp);
        }
    }
}

