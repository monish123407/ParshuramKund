package com.parshuramKund.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parshuramKund.DTO.ApplicantDTO;
import com.parshuramKund.Entity.Applicant;
import com.parshuramKund.Repository.ApplicantRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ApplicantServiceimpl implements ApplicantService {
	
	
	 @Autowired
	private  ApplicantRepository applicantRepository ;

	 @Autowired
	private EmailService emailService;

	@Override
	public ApplicantDTO registerUser(ApplicantDTO applicantDTO) {
		// TODO Auto-generated method stub
		
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String formattedDate = now.format(formatter);
		
		Applicant applicant=new Applicant();
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
	public void deleteRegistration(Long id) {
		java.util.Optional<Applicant> applicantOptional = applicantRepository.findById(id);
		if (applicantOptional.isPresent()) {
			Applicant applicant = applicantOptional.get();
			
			// Delete Aadhaar photo upload
			if (applicant.getAadharPhotoPath() != null && !applicant.getAadharPhotoPath().isEmpty()) {
				try {
					java.nio.file.Path aadharPath = java.nio.file.Paths.get("aadhar-photos").resolve(applicant.getAadharPhotoPath()).toAbsolutePath().normalize();
					java.io.File file = aadharPath.toFile();
					if (file.exists() && file.isFile()) {
						file.delete();
					}
				} catch (Exception e) {
					// Ignore deletion failures
				}
			}
			
			// Delete cached PDF pass
			try {
				java.nio.file.Path pdfPath = java.nio.file.Paths.get("generated-passes").resolve("Pass_" + id + ".pdf").toAbsolutePath().normalize();
				java.io.File file = pdfPath.toFile();
				if (file.exists() && file.isFile()) {
					file.delete();
				}
			} catch (Exception e) {
				// Ignore deletion failures
			}
			
			applicantRepository.delete(applicant);
		}
	}

	@Override
	public List<ApplicantDTO> search(String query) {
		List<Applicant> applicantARRAY = new java.util.ArrayList<>();
		
		// 1. Try to parse as ID
		try {
			Long id = Long.parseLong(query);
			applicantRepository.findById(id).ifPresent(applicantARRAY::add);
		} catch (NumberFormatException e) {
			// Not a numeric ID, ignore
		}
		
		// 2. Search by phone
		applicantARRAY.addAll(applicantRepository.findByPhone(query));
		
		// 3. Search by email (case-insensitive)
		applicantARRAY.addAll(applicantRepository.findByEmailIgnoreCase(query));
		
		// 4. Search by name containing (case-insensitive)
		applicantARRAY.addAll(applicantRepository.findByFullNameContainingIgnoreCase(query));
		
		// Deduplicate results by ID
		List<Applicant> uniqueApplicants = new java.util.ArrayList<>();
		List<Long> seenIds = new java.util.ArrayList<>();
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
	public ApplicantDTO findById(Long id) {
		return applicantRepository.findById(id)
				.map(entity -> {
					ApplicantDTO dto = new ApplicantDTO();
					return dto.EntityToDTO(entity);
				})
				.orElse(null);
	}
}

