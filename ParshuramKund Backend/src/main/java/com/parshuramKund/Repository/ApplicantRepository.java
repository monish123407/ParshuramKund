package com.parshuramKund.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.parshuramKund.Entity.Applicant;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, String> {

	List<Applicant> findByPhone(String mobileNo);

	List<Applicant> findByEmailIgnoreCase(String email);

	List<Applicant> findByFullNameContainingIgnoreCase(String fullName);
}
