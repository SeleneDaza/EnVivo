package com.edu.uptc.EnVivo.config;

import com.edu.uptc.EnVivo.repository.UserRepository;
import com.edu.uptc.EnVivo.logging.StructuredLogContext;
import com.edu.uptc.EnVivo.logging.StructuredLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.edu.uptc.EnVivo.entity.Role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final StructuredLogService structuredLogService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        InMemoryUserDetailsManager inMemory = createInMemoryManager(passwordEncoder);

        return username -> {
            if (inMemory.userExists(username)) {
                return inMemory.loadUserByUsername(username);
            }
            return loadUserFromDatabase(username);
        };
    }

    private InMemoryUserDetailsManager createInMemoryManager(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("1234"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    private UserDetails loadUserFromDatabase(String username) {
        com.edu.uptc.EnVivo.entity.User user = userRepository.findByUserName(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        String principalName = user.getUserName() != null && !user.getUserName().isBlank()
                ? user.getUserName()
                : user.getEmail();

        return new org.springframework.security.core.userdetails.User(
                principalName,
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles())
        );
    }

    private List<SimpleGrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                
                        .requestMatchers("/", "/login", "/register","/css/**", "/js/**", "/images/**", "/ws/**").permitAll()
                        .requestMatchers("/api/eventos/**").permitAll()
                        .requestMatchers("/sales", "/api/sales/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true) 
                    .successHandler(this::handleLoginSuccess)
                    .failureHandler(this::handleLoginFailure)
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

    private void handleLoginSuccess(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication)
            throws IOException {
        String transactionId = StructuredLogContext.currentTransactionId();
        String clientIp = StructuredLogContext.currentClientIp();
        String userId = authentication.getName();

        try (StructuredLogContext.Scope ignored = structuredLogService.scope(Map.of(
                StructuredLogContext.KEY_USER_ID, userId
        ))) {
            structuredLogService.logSuccess("auth", "AUTH_LOGIN_SUCCESS", transactionId, null, userId,
                    clientIp, null, "SUCCESS", "User authenticated successfully.");
        }

        response.sendRedirect("/");
    }

    private void handleLoginFailure(HttpServletRequest request, HttpServletResponse response,
                                    org.springframework.security.core.AuthenticationException exception)
            throws IOException {
        String transactionId = StructuredLogContext.currentTransactionId();
        String clientIp = StructuredLogContext.currentClientIp();

        structuredLogService.logError("auth", "AUTH_LOGIN_FAILED", transactionId, null, null,
                clientIp, null, "AUTH_INVALID_CREDENTIALS",
                "Authentication failed: " + exception.getClass().getSimpleName() + ": " + exception.getMessage(),
                "Invalid credentials or unavailable account.", null);

        response.sendRedirect("/login?error=true");
    }
}