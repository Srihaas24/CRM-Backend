package com.crm.BackendApp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.verification-url=http://localhost:8080/verify",
		"app.password-reset-url=http://localhost:8080/reset-password",
		"spring.mail.username=test@example.com",
		"spring.mail.password=testpassword"
})
class BackendAppApplicationTests {

	static {
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
	}

	@Test
	void contextLoads() {
	}

}
