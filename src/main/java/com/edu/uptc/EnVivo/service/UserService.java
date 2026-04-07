package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.RegisterDTO;
import com.edu.uptc.EnVivo.entity.Role;
import com.edu.uptc.EnVivo.entity.User;
import com.edu.uptc.EnVivo.repository.RoleRepository;
import com.edu.uptc.EnVivo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean registerUser(RegisterDTO dto) {
        String login = normalizeLogin(dto.getNewUsername());
        if (login == null) {
            return false;
        }

        if (!arePasswordsMatching(dto)) {
            return false;
        }

        if (userExists(login)) {
            return false;
        }

        Role rolCliente = getOrCreateClientRole();
        saveNewUser(login, dto.getNewPassword(), rolCliente);
        return true;
    }

    private boolean arePasswordsMatching(RegisterDTO dto) {
        return dto.getNewPassword().equals(dto.getConfirmPassword());
    }

    private String normalizeLogin(String login) {
        if (login == null) {
            return null;
        }
        String normalized = login.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean userExists(String login) {
        return userRepository.existsByUserName(login) || userRepository.existsByEmail(login);
    }

    private Role getOrCreateClientRole() {
        return roleRepository.findByName("CLIENTE")
                .orElseGet(this::createClientRole);
    }

    private Role createClientRole() {
        Role nuevoRol = new Role();
        nuevoRol.setName("CLIENTE");
        return roleRepository.save(nuevoRol);
    }

    private void saveNewUser(String login, String rawPassword, Role role) {
        User user = new User();
        user.setUserName(login);
        // Mantiene compatibilidad con esquemas antiguos donde email es requerido.
        user.setEmail(login);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }
    
    public List<User> getClientUsers() {
        return userRepository.findUsersWithoutAdminRole();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
}

