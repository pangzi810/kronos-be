package com.devhour.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

/**
 * Test Application Context Initializer
 * 
 * This initializer runs before the Spring context is created and
 * sets system properties to completely disable Okta processing
 */
public class TestApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Set system properties to completely disable Okta
        System.setProperty("security.okta.enabled", "false");
        System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", "");
        System.setProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", "");
        System.setProperty("okta.oauth2.issuer", "");
        System.setProperty("spring.security.oauth2.resourceserver.jwt.client-id", "");
        
        // Add test properties programmatically
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
            "security.okta.enabled=false",
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=",
            "okta.oauth2.issuer=",
            "spring.security.oauth2.resourceserver.jwt.client-id=",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,com.okta.spring.boot.oauth.OktaOAuth2AutoConfiguration"
        );
    }
}