package com.parshuramKund.Service;

import com.parshuramKund.DTO.ApplicantDTO;

public interface EmailService {
    void sendRegistrationEmail(ApplicantDTO applicant);
    void sendInquiryReplyEmail(String recipientEmail, String userName, String inquirySubject, String inquiryMessage, String replyMessage);
    void sendOtpEmail(String recipientEmail, String otp);
}

