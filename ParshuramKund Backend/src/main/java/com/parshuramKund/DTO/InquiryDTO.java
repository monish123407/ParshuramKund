package com.parshuramKund.DTO;

import com.parshuramKund.Entity.Inquiry;

public class InquiryDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private String submittedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public InquiryDTO EntityToDTO(Inquiry inquiry) {
        InquiryDTO dto = new InquiryDTO();
        dto.setId(inquiry.getId());
        dto.setName(inquiry.getName());
        dto.setEmail(inquiry.getEmail());
        dto.setPhone(inquiry.getPhone());
        dto.setSubject(inquiry.getSubject());
        dto.setMessage(inquiry.getMessage());
        dto.setSubmittedAt(inquiry.getSubmittedAt());
        return dto;
    }

    @Override
    public String toString() {
        return "InquiryDTO [id=" + id + ", name=" + name + ", email=" + email + ", phone=" + phone + ", subject="
                + subject + ", message=" + message + ", submittedAt=" + submittedAt + "]";
    }
}
