package com.devhour;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({TestRepositoryConfiguration.class, com.devhour.config.TestSecurityConfiguration.class})
@ActiveProfiles("test")
class DevelopmentHourManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
