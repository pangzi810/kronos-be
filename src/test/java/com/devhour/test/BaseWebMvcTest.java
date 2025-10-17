package com.devhour.test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.devhour.config.TestSecurityConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base annotation for Web MVC tests with unified security configuration
 * 
 * This annotation provides:
 * - Consistent test profile activation
 * - Unified security configuration import
 * - MockMvc auto-configuration with filters disabled
 * 
 * Usage:
 * @BaseWebMvcTest
 * @WebMvcTest(YourController.class)
 * class YourControllerTest {
 *   // Your test methods
 * }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public @interface BaseWebMvcTest {
}