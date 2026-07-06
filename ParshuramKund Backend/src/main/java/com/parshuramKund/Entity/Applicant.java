package com.parshuramKund.Entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class Applicant {

	 	@Id
	    private String id;

	    @Column(nullable = false)
	    private String fullName;
	    
	    @Column(nullable = false)
	    private String dateOfHoliDip;
	    
	    
	    @Column(nullable = false)
	    private Long age;
	    
	    @Column(nullable = false)
	    private String comorbidities;
	    

	    @Column(nullable = false)
	    private String email;

	    @Column(nullable = false)
	    private String phone;

	    @Column(nullable = false)
	    private String gender;
	    
	    @Column(nullable = false)
	    private String presentAddress;
	    
	    @Column(nullable = false)
	    private String presentState;
	    
	    @Column(nullable = false)
	    private String bookingDate;
	    
	    @Column(nullable = false)
	    private String presentDistrict;
	    
	    @Column(nullable = false)
	    private String presentPinCode;
	    
	    @Column(nullable = false)
	    private String permanentAddress;
	    
	    @Column(nullable = false)
	    private String permanentState;
	    
	    @Column(nullable = false)
	    private String permanentDistrict;
	    
	    @Column(nullable = false)
	    private String permanentPinCode;
	    

	    private String coApplicant;
	    
	    private Boolean isPresentCoApplicant;
	    
	    private String aadharNumber;
	    
	    private String aadharPhotoPath;

	    @Column(nullable = false)
	    private Boolean verified = false;

	    @Column(nullable = false)
	    private Boolean rejected = false;

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

	    private String verifiedBy;

	    private String verifiedAt;

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


	    
	    
		public Boolean getIsPresentCoApplicant() {
			return isPresentCoApplicant;
		}

		public void setIsPresentCoApplicant(Boolean isPresentCoApplicant) {
			this.isPresentCoApplicant = isPresentCoApplicant;
		}

		public String getDateOfHoliDip() {
			return dateOfHoliDip;
		}

		public void setDateOfHoliDip(String dateOfHoliDip) {
			this.dateOfHoliDip = dateOfHoliDip;
		}

		public String getBookingDate() {
			return bookingDate;
		}

		public void setBookingDate(String bookingDate) {
			this.bookingDate = bookingDate;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getDateOfHoliDipDate() {
			return dateOfHoliDip;
		}

		public void setDateOfHoliDipDate(String dateOfHolidayDip) {
			this.dateOfHoliDip = dateOfHolidayDip;
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
			return coApplicant;
		}

		public void setCoApplicant(String coApplicant) {
			this.coApplicant = coApplicant;
		}
	    
	    
	    
	   
	    
	  
	   
	    
	    
}
