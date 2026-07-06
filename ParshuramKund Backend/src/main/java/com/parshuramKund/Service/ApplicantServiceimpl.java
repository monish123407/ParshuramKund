package com.parshuramKund.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parshuramKund.DTO.ApplicantDTO;
import com.parshuramKund.Entity.Applicant;
import com.parshuramKund.Entity.AdminUser;
import com.parshuramKund.Repository.ApplicantRepository;
import com.parshuramKund.Repository.AdminUserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ApplicantServiceimpl implements ApplicantService {
	
	
	 @Autowired
	private  ApplicantRepository applicantRepository ;

	 @Autowired
	private EmailService emailService;

	 @Autowired
	private AdminUserRepository adminUserRepository;

	@Override
	public ApplicantDTO registerUser(ApplicantDTO applicantDTO) {
		// TODO Auto-generated method stub
		
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String formattedDate = now.format(formatter);
		
		Applicant applicant=new Applicant();
		
		// Generate custom 16-digit ID using booking datetime (yyMMddHHmmss) + last 4 digits of phone
		DateTimeFormatter idFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
		String datetimeStr = now.format(idFormatter);
		String phone = applicantDTO.getPhone() != null ? applicantDTO.getPhone().trim() : "";
		String last4Digits = "0000";
		if (phone.length() >= 4) {
			last4Digits = phone.substring(phone.length() - 4);
		} else if (!phone.isEmpty()) {
			try {
				last4Digits = String.format("%04d", Integer.parseInt(phone));
			} catch (NumberFormatException e) {
				// fallback if not fully numeric
				last4Digits = "0000";
			}
		}
		
		Long generatedId = Long.parseLong(datetimeStr + last4Digits);
		// Resilient loop to ensure absolute uniqueness under concurrent scenarios
		while (applicantRepository.existsById(generatedId.toString())) {
			generatedId++;
		}
		applicant.setId(generatedId.toString());

		applicant.setFullName(applicantDTO.getFullName());
		applicant.setDateOfHoliDipDate(applicantDTO.getHolyDipDate());
		applicant.setAge(applicantDTO.getAge());
		applicant.setComorbidities(applicantDTO.getComorbidities());
		applicant.setPhone(applicantDTO.getPhone());
		applicant.setEmail(applicantDTO.getEmail());
		applicant.setGender(applicantDTO.getGender());
		applicant.setPresentAddress(applicantDTO.getPresentAddress());
		applicant.setPresentState(applicantDTO.getPresentState());
		applicant.setPresentPinCode(applicantDTO.getPresentPinCode());
		applicant.setPresentDistrict(applicantDTO.getPresentDistrict());
		applicant.setPermanentAddress(applicantDTO.getPermanentAddress());
		applicant.setPermanentState(applicantDTO.getPermanentState());
		applicant.setPermanentPinCode(applicantDTO.getPermanentPinCode());
		applicant.setPermanentDistrict(applicantDTO.getPermanentDistrict());
		applicant.setCoApplicant(applicantDTO.getCoApplicant());
		if ("Yes".equals(applicantDTO.getIsPresentCoApplicant()))
			applicant.setIsPresentCoApplicant(true);
		else
			applicant.setIsPresentCoApplicant(false);
		applicant.setBookingDate(formattedDate);
		if (applicantDTO.getAadharNumber() != null && !applicantDTO.getAadharNumber().isEmpty()) {
			applicant.setAadharNumber(com.parshuramKund.Util.AadharEncryptionUtil.encrypt(applicantDTO.getAadharNumber()));
		}
		applicant.setAadharPhotoPath(applicantDTO.getAadharPhotoPath());
		applicantRepository.save(applicant);
		applicantDTO=applicantDTO.EntityToDTO(applicant);

		try {
			emailService.sendRegistrationEmail(applicantDTO);
		} catch (Exception e) {
			// Gracefully handle any email service exceptions to avoid registration failure
		}

		return applicantDTO;
	}

	@Override
	public List<ApplicantDTO> findByPhone(String mobile) {
		// TODO Auto-generated method stub
		
		List<Applicant> applicantARRAY= applicantRepository.findByPhone(mobile);
		
		List<ApplicantDTO> applicantDTOARRAY=new ArrayList<ApplicantDTO>();
		for (Applicant entity : applicantARRAY) {

			ApplicantDTO dto = new ApplicantDTO();

		  

			applicantDTOARRAY.add(dto.EntityToDTO(entity));
		}
        
		return applicantDTOARRAY;
	}

	@Override
	public List<ApplicantDTO> findAllRegistrations() {
		List<Applicant> applicantARRAY = applicantRepository.findAll();
		List<ApplicantDTO> applicantDTOARRAY = new ArrayList<ApplicantDTO>();
		for (Applicant entity : applicantARRAY) {
			ApplicantDTO dto = new ApplicantDTO();
			applicantDTOARRAY.add(dto.EntityToDTO(entity));
		}
		return applicantDTOARRAY;
	}

	@Override
	public void deleteRegistration(String id) {
		applicantRepository.deleteById(id);
	}

	@Override
	public List<ApplicantDTO> search(String query) {
		List<Applicant> applicantARRAY = new java.util.ArrayList<>();
		
		// 1. Search by ID directly as String
		applicantRepository.findById(query).ifPresent(applicantARRAY::add);
		
		// 2. Search by phone
		applicantARRAY.addAll(applicantRepository.findByPhone(query));
		
		// 3. Search by email (case-insensitive)
		applicantARRAY.addAll(applicantRepository.findByEmailIgnoreCase(query));
		
		// 4. Search by name containing (case-insensitive)
		applicantARRAY.addAll(applicantRepository.findByFullNameContainingIgnoreCase(query));
		
		// Deduplicate results by ID
		List<Applicant> uniqueApplicants = new java.util.ArrayList<>();
		List<String> seenIds = new java.util.ArrayList<>();
		for (Applicant a : applicantARRAY) {
			if (!seenIds.contains(a.getId())) {
				seenIds.add(a.getId());
				uniqueApplicants.add(a);
			}
		}
		
		// Map to DTO
		List<ApplicantDTO> applicantDTOARRAY = new java.util.ArrayList<>();
		for (Applicant entity : uniqueApplicants) {
			ApplicantDTO dto = new ApplicantDTO();
			applicantDTOARRAY.add(dto.EntityToDTO(entity));
		}
		
		return applicantDTOARRAY;
	}

	@Override
	public ApplicantDTO findById(String id) {
		return applicantRepository.findById(id)
				.map(entity -> {
					ApplicantDTO dto = new ApplicantDTO();
					return dto.EntityToDTO(entity);
				})
				.orElse(null);
	}

	@Override
	public ApplicantDTO verifyRegistration(String id, String verifiedByUsername) {
		Applicant applicant = applicantRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Registration ID not found"));
		applicant.setVerified(true);
		applicant.setRejected(false); // Make sure it's not rejected if verified
		
		String verifiedByName = "System";
		if (verifiedByUsername != null) {
			Optional<AdminUser> adminOpt = adminUserRepository.findByUsername(verifiedByUsername);
			if (adminOpt.isPresent()) {
				verifiedByName = adminOpt.get().getFullName();
			} else {
				verifiedByName = verifiedByUsername;
			}
		}
		applicant.setVerifiedBy(verifiedByName);
		
		String formattedDate = java.time.LocalDateTime.now()
				.format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
		applicant.setVerifiedAt(formattedDate);
		
		applicantRepository.save(applicant);
		ApplicantDTO dto = new ApplicantDTO();
		return dto.EntityToDTO(applicant);
	}

	@Override
	public ApplicantDTO rejectRegistration(String id, String rejectedByUsername) {
		Applicant applicant = applicantRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Registration ID not found"));
		applicant.setRejected(true);
		applicant.setVerified(false); // Cannot be verified if rejected
		
		String rejectedByName = "System";
		if (rejectedByUsername != null) {
			Optional<AdminUser> adminOpt = adminUserRepository.findByUsername(rejectedByUsername);
			if (adminOpt.isPresent()) {
				rejectedByName = adminOpt.get().getFullName();
			} else {
				rejectedByName = rejectedByUsername;
			}
		}
		applicant.setVerifiedBy(rejectedByName);
		
		String formattedDate = java.time.LocalDateTime.now()
				.format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
		applicant.setVerifiedAt(formattedDate);
		
		applicantRepository.save(applicant);
		ApplicantDTO dto = new ApplicantDTO();
		return dto.EntityToDTO(applicant);
	}
}

