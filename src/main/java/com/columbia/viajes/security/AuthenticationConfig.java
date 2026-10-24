package com.columbia.viajes.security;

import com.columbia.viajes.security.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationConfig {
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService::loadUserByIdentificador);
        
        provider.setPasswordEncoder(passwordEncoder);
        
        return provider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider authenticationProvider){
        return new ProviderManager(authenticationProvider);
    }
    
}
