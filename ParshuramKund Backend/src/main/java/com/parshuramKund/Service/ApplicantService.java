package com.parshuramKund.Service;

import java.util.List;

import com.parshuramKund.DTO.ApplicantDTO;

public interface ApplicantService {
	
	public ApplicantDTO registerUser(ApplicantDTO applicantDTO);
	public List<ApplicantDTO> findByPhone(String mobile);
	public List<ApplicantDTO> findAllRegistrations();
	public void deleteRegistration(Long id);
	public List<ApplicantDTO> search(String query);
	public ApplicantDTO findById(Long id);

}
