package com.edu.uptc.EnVivo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests((requests) -> requests
            // Rutas públicas
            .requestMatchers("/", "/index", "/register", "/css/**", "/js/**", "/images/**").permitAll()
            
            // RESTRICCIÓN: Solo usuarios con rol ADMIN pueden entrar aquí
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/categories/**").hasRole("ADMIN")
            
            // El resto requiere estar logueado
            .anyRequest().authenticated()
        )
        .formLogin((form) -> form
            .loginPage("/")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/main", true) 
            .permitAll()
        )
        .logout((logout) -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .permitAll()
        )
        .csrf(csrf -> csrf.disable()); 

    return http.build();
}

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("admin")
                .password("{noop}admin123")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}