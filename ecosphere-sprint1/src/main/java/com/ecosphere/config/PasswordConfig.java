package com.ecosphere.config;
import org.springframework.context.annotation.*; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;

/** Separate from SecurityConfig so AuthService (needed by the login handler) can depend on the encoder without a circular bean reference. */
@Configuration
public class PasswordConfig { @Bean PasswordEncoder encoder(){ return new BCryptPasswordEncoder(); } }
