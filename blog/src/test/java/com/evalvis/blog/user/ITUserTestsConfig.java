package com.evalvis.blog.user;

import com.evalvis.blog.Email;
import com.evalvis.blog.FakeSmtpEmail;
import com.evalvis.blog.SecurityConfig;
import com.evalvis.blog.logging.HttpLoggingFilter;
import com.evalvis.security.JwtKey;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ComponentScan("com.evalvis.security")
@Import(value = {
        UserController.class, SecurityConfig.class, UserDetailsServiceImpl.class, HttpLoggingFilter.class, JwtKey.class
})
public class ITUserTestsConfig {
    @Bean
    public UserRepository fakeUserRepository() {
        return new FakeUserRepository();
    }

    @Bean
    public PasswordResetRepository fakePasswordResetRepository() {
        return new FakePasswordResetRepository();
    }

    @Bean
    public Email fakeSmtpEmail() {
        return new FakeSmtpEmail();
    }

    @Bean
    public ClientRegistrationRepository fakeClientRegistrationRepository() {
        return new FakeClientRegistrationRepository();
    }
}
