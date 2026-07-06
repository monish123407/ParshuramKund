package com.parshuramKund.DTO;

import java.util.Date;

import com.parshuramKund.Entity.Applicant;

import jakarta.persistence.Column;


public class ApplicantDTO {
	
	
	private String id;
 
    private String fullName;
    
    
    private String holyDipDate;
    
    
   
    private Long age;
    
   
    private String comorbidities;

    
    private String email;

    
    private String phone;

    
    private String gender;
    
    
    private String presentAddress;
    
    
    private String presentState;
    
    
    private String presentDistrict;
    
   
    private String presentPinCode;
    
    
    private String permanentAddress;
    
    
    private String permanentState;
    
   
    private String permanentDistrict;
    
    
    private String permanentPinCode;
    

    private String coTraveller;
    
    private String bookingDate;
    
    private String isPresentCoApplicant;
    
    private String aadharNumber;
    
    private String aadharPhotoPath;
    private Boolean verified = false;
    private Boolean rejected = false;
    private String verifiedBy;
    private String verifiedAt;

	public String getAadharNumber() {
		return aadharNumber;
	}


	public void setAadharNumber(String aadharNumber) {
		this.aadharNumber = aadharNumber;
	}


	public String getAadharPhotoPath() {
		return aadharPhotoPath;
	}


	public void setAadharPhotoPath(String aadharPhotoPath) {
		this.aadharPhotoPath = aadharPhotoPath;
	}

	public Boolean getVerified() {
		return verified;
	}

	public void setVerified(Boolean verified) {
		this.verified = verified;
	}

	public Boolean getRejected() {
		return rejected;
	}

	public void setRejected(Boolean rejected) {
		this.rejected = rejected;
	}

	public String getVerifiedBy() {
		return verifiedBy;
	}

	public void setVerifiedBy(String verifiedBy) {
		this.verifiedBy = verifiedBy;
	}

	public String getVerifiedAt() {
		return verifiedAt;
	}

	public void setVerifiedAt(String verifiedAt) {
		this.verifiedAt = verifiedAt;
	}
    
    
    


	public String getIsPresentCoApplicant() {
		return isPresentCoApplicant;
	}


	public void setIsPresentCoApplicant(String isPresentCoApplicant) {
		this.isPresentCoApplicant = isPresentCoApplicant;
	}


	public String getBookingDate() {
		return bookingDate;
	}


	public void setBookingDate(String bookingDate) {
		this.bookingDate = bookingDate;
	}


	public String getFullName() {
		return fullName;
	}


	public void setFullName(String fullName) {
		this.fullName = fullName;
	}


	public String getHolyDipDate() {
		return holyDipDate;
	}


	public void setHolyDipDate(String dateOfHolidayDip) {
		this.holyDipDate = dateOfHolidayDip;
	}


	public Long getAge() {
		return age;
	}


	public void setAge(Long age) {
		this.age = age;
	}


	public String getComorbidities() {
		return comorbidities;
	}


	public void setComorbidities(String comorbidities) {
		this.comorbidities = comorbidities;
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


	public String getGender() {
		return gender;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	public String getPresentAddress() {
		return presentAddress;
	}


	public void setPresentAddress(String presentAddress) {
		this.presentAddress = presentAddress;
	}


	public String getPresentState() {
		return presentState;
	}


	public void setPresentState(String presentState) {
		this.presentState = presentState;
	}


	public String getPresentDistrict() {
		return presentDistrict;
	}


	public void setPresentDistrict(String presentDistrict) {
		this.presentDistrict = presentDistrict;
	}


	public String getPresentPinCode() {
		return presentPinCode;
	}


	public void setPresentPinCode(String presentPinCode) {
		this.presentPinCode = presentPinCode;
	}


	public String getPermanentAddress() {
		return permanentAddress;
	}


	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
	}


	public String getPermanentState() {
		return permanentState;
	}


	public void setPermanentState(String permanentState) {
		this.permanentState = permanentState;
	}


	public String getPermanentDistrict() {
		return permanentDistrict;
	}


	public void setPermanentDistrict(String permanentDistrict) {
		this.permanentDistrict = permanentDistrict;
	}


	public String getPermanentPinCode() {
		return permanentPinCode;
	}


	public void setPermanentPinCode(String permanentPinCode) {
		this.permanentPinCode = permanentPinCode;
	}


	public String getCoApplicant() {
		return coTraveller;
	}


	public void setCoApplicant(String coTraveller) {
		this.coTraveller = coTraveller;
	}
	
	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getCoTraveller() {
		return coTraveller;
	}


	public void setCoTraveller(String coTraveller) {
		this.coTraveller = coTraveller;
	}


	public ApplicantDTO EntityToDTO(Applicant applicant) {
		ApplicantDTO applicantDTO=new ApplicantDTO();
		applicantDTO.setId(applicant.getId());
		applicantDTO.setFullName(applicant.getFullName());
		applicantDTO.setHolyDipDate(applicant.getDateOfHoliDipDate());
		applicantDTO.setAge(applicant.getAge());
		applicantDTO.setComorbidities(applicant.getComorbidities());
		applicantDTO.setPhone(applicant.getPhone());
		applicantDTO.setEmail(applicant.getEmail());
		applicantDTO.setGender(applicant.getGender());
		applicantDTO.setPresentAddress(applicant.getPresentAddress());
		applicantDTO.setPresentState(applicant.getPresentState());
		applicantDTO.setPresentPinCode(applicant.getPresentPinCode());
		applicantDTO.setPresentDistrict(applicant.getPresentDistrict());
		applicantDTO.setPermanentAddress(applicant.getPermanentAddress());
		applicantDTO.setPermanentState(applicant.getPermanentState());
		applicantDTO.setPermanentPinCode(applicant.getPermanentPinCode());
		applicantDTO.setPermanentDistrict(applicant.getPermanentDistrict());
		applicantDTO.setBookingDate(applicant.getBookingDate());
		applicantDTO.setCoApplicant(applicant.getCoApplicant());
		if (applicant.getIsPresentCoApplicant() != null) {
			applicantDTO.setIsPresentCoApplicant(applicant.getIsPresentCoApplicant() ? "Yes" : "No");
		} else {
			applicantDTO.setIsPresentCoApplicant("No");
		}
		if (applicant.getAadharNumber() != null && !applicant.getAadharNumber().isEmpty()) {
			try {
				String decrypted = com.parshuramKund.Util.AadharEncryptionUtil.decrypt(applicant.getAadharNumber());
				applicantDTO.setAadharNumber(com.parshuramKund.Util.AadharEncryptionUtil.mask(decrypted));
			} catch (Exception e) {
				applicantDTO.setAadharNumber("XXXX-XXXX-XXXX");
			}
		}
		applicantDTO.setAadharPhotoPath(applicant.getAadharPhotoPath());
		applicantDTO.setVerified(applicant.getVerified());
		applicantDTO.setRejected(applicant.getRejected());
		applicantDTO.setVerifiedBy(applicant.getVerifiedBy());
		applicantDTO.setVerifiedAt(applicant.getVerifiedAt());
		return applicantDTO;
		
	}


	@Override
	public String toString() {
		return "ApplicantDTO [id=" + id + ", fullName=" + fullName + ", holyDipDate=" + holyDipDate + ", age=" + age
				+ ", comorbidities=" + comorbidities + ", email=" + email + ", phone=" + phone + ", gender=" + gender
				+ ", presentAddress=" + presentAddress + ", presentState=" + presentState + ", presentDistrict="
				+ presentDistrict + ", presentPinCode=" + presentPinCode + ", permanentAddress=" + permanentAddress
				+ ", permanentState=" + permanentState + ", permanentDistrict=" + permanentDistrict
				+ ", permanentPinCode=" + permanentPinCode + ", coTraveller=" + coTraveller + ", bookingDate="
				+ bookingDate + ", isPresentCoApplicant=" + isPresentCoApplicant + "]";
	}

	
    
    

}
