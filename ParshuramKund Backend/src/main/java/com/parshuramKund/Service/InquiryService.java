package com.parshuramKund.Service;

import java.util.List;
import com.parshuramKund.DTO.InquiryDTO;

public interface InquiryService {
    InquiryDTO saveInquiry(InquiryDTO dto);
    List<InquiryDTO> getAllInquiries();
    void deleteInquiry(Long id);
    void disposeInquiryWithMessage(Long id, String replyMessage);
}
