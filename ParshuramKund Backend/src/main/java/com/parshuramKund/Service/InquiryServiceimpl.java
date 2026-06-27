package com.parshuramKund.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parshuramKund.DTO.InquiryDTO;
import com.parshuramKund.Entity.Inquiry;
import com.parshuramKund.Repository.InquiryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryServiceimpl implements InquiryService {

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public InquiryDTO saveInquiry(InquiryDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        String formattedDate = now.format(formatter);

        Inquiry inquiry = new Inquiry();
        inquiry.setName(dto.getName());
        inquiry.setEmail(dto.getEmail());
        inquiry.setPhone(dto.getPhone());
        inquiry.setSubject(dto.getSubject());
        inquiry.setMessage(dto.getMessage());
        inquiry.setSubmittedAt(formattedDate);

        inquiryRepository.save(inquiry);

        return dto.EntityToDTO(inquiry);
    }

    @Override
    public List<InquiryDTO> getAllInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findAll();
        List<InquiryDTO> dtos = new ArrayList<>();
        for (Inquiry inquiry : inquiries) {
            InquiryDTO dto = new InquiryDTO();
            dtos.add(dto.EntityToDTO(inquiry));
        }
        return dtos;
    }

    @Override
    public void deleteInquiry(Long id) {
        inquiryRepository.deleteById(id);
    }

    @Override
    public void disposeInquiryWithMessage(Long id, String replyMessage) {
        java.util.Optional<Inquiry> opt = inquiryRepository.findById(id);
        if (opt.isPresent()) {
            Inquiry inquiry = opt.get();
            emailService.sendInquiryReplyEmail(
                inquiry.getEmail(),
                inquiry.getName(),
                inquiry.getSubject(),
                inquiry.getMessage(),
                replyMessage
            );
            inquiryRepository.deleteById(id);
        } else {
            throw new RuntimeException("Inquiry not found with ID: " + id);
        }
    }
}
