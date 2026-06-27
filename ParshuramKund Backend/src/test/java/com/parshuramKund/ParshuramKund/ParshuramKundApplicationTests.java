package com.parshuramKund.ParshuramKund;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
class ParshuramKundApplicationTests {

	@Autowired
	private com.parshuramKund.Repository.ApplicantRepository repository;

	@Test
	void contextLoads() {
	}

	@Test
	void verifyAadharStorage() {
		java.util.List<com.parshuramKund.Entity.Applicant> list = repository.findAll();
		boolean foundTest = false;
		for (com.parshuramKund.Entity.Applicant app : list) {
			if (app.getAadharNumber() != null && !app.getAadharNumber().isEmpty()) {
				String dbAadhar = app.getAadharNumber();
				// Ensure it's not the plain text "123456789012"
				org.junit.jupiter.api.Assertions.assertNotEquals("123456789012", dbAadhar);
				
				// Ensure it decrypts back to "123456789012"
				String decrypted = com.parshuramKund.Util.AadharEncryptionUtil.decrypt(dbAadhar);
				if ("123456789012".equals(decrypted)) {
					foundTest = true;
				}
			}
		}
		// Confirm at least one test record was verified
		org.junit.jupiter.api.Assertions.assertTrue(foundTest, "No encrypted test record with plain text '123456789012' was found in the database.");
	}

}
