package com.evalvis.blog;

import com.evalvis.blog.logging.HttpLoggingFilter;
import com.evalvis.blog.user.JwtFilter;
import com.evalvis.blog.user.OAuth2AuthorizationSuccessHandler;
import com.evalvis.blog.user.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private HttpLoggingFilter loggingFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(
                        exception -> exception.authenticationEntryPoint(
                                (request, response, authException) -> {
                                    logger.error("Unauthorized error: {}", authException.getMessage());
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
                                }
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        requests -> requests
                                .requestMatchers("/users/signup", "/users/login", "/actuator/prometheus")
                                .permitAll()
                                .requestMatchers(HttpMethod.OPTIONS)
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2AuthorizationSuccessHandler()));
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public OAuth2AuthorizationSuccessHandler oAuth2AuthorizationSuccessHandler() {
        return new OAuth2AuthorizationSuccessHandler();
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}