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
                // Permitimos el index (login), el registro y los archivos estáticos
                .requestMatchers("/", "/index", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                // Cualquier otra ruta (como /main, /admin, /categories) requiere login
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/") // Tu archivo index.html es la raíz
                .loginProcessingUrl("/login") // Esta es la URL que Spring Security escuchará
                .defaultSuccessUrl("/main", true) // Al entrar con éxito, va a la cartelera
                .failureUrl("/?error=true") // Si falla, vuelve al index con un parámetro de error
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // Deshabilitamos CSRF temporalmente para facilitar las pruebas con tus formularios actuales
            .csrf(csrf -> csrf.disable()); 

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("admin")
                .password("{noop}admin123") // {noop} es para contraseñas en texto plano (solo desarrollo)
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}