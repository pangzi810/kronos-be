package com.devhour.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Test Web MVC Configuration
 * 
 * This configuration ensures consistent setup for @WebMvcTest classes:
 * - Imports unified test security configuration
 * - Provides component scanning for test-specific components
 * - Active only during test profile
 */
@TestConfiguration
@Profile("test")
@Import(TestSecurityConfiguration.class)
@ComponentScan(basePackages = {
    "com.devhour.config",
    "com.devhour.presentation.controller"
})
public class TestWebMvcConfiguration {
    // This configuration class primarily serves as a central import point
    // for test-specific configurations and security settings
}