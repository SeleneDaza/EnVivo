package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.RegisterDTO;
import com.edu.uptc.EnVivo.dto.UpdateProfileDTO;
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
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    public enum UpdateProfileResult {
        SUCCESS,
        USER_NOT_FOUND,
        INVALID_FULL_NAME,
        INVALID_DOCUMENT,
        INVALID_PHONE,
        INVALID_EMAIL,
        EMAIL_IN_USE,
        DOCUMENT_IN_USE
    }

    private static final Pattern FULL_NAME_PATTERN =
            Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{3,100}$");
    private static final Pattern DOCUMENT_PATTERN =
            Pattern.compile("^\\d{6,15}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{7,15}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

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

    public UpdateProfileResult updatePersonalInfo(String principalName, UpdateProfileDTO dto) {
        User user = findByUserName(principalName)
                .or(() -> findByEmail(principalName))
                .orElse(null);

        if (user == null) {
            return UpdateProfileResult.USER_NOT_FOUND;
        }

        String fullName = normalizeFullName(dto.getFullName());
        String document = normalizeRequiredField(dto.getDocument());
        String email = normalizeRequiredField(dto.getEmail());
        String phone = normalizeRequiredField(dto.getPhone());

        if (fullName == null || !FULL_NAME_PATTERN.matcher(fullName).matches()) {
            return UpdateProfileResult.INVALID_FULL_NAME;
        }

        if (document == null || !DOCUMENT_PATTERN.matcher(document).matches()) {
            return UpdateProfileResult.INVALID_DOCUMENT;
        }

        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            return UpdateProfileResult.INVALID_PHONE;
        }

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return UpdateProfileResult.INVALID_EMAIL;
        }

        Optional<User> emailOwner = userRepository.findByEmail(email);
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(user.getId())) {
            return UpdateProfileResult.EMAIL_IN_USE;
        }

        Optional<User> documentOwner = userRepository.findByDocument(document);
        if (documentOwner.isPresent() && !documentOwner.get().getId().equals(user.getId())) {
            return UpdateProfileResult.DOCUMENT_IN_USE;
        }

        user.setFullName(fullName);
        user.setDocument(document);
        user.setPhone(phone);
        user.setEmail(email);
        userRepository.save(user);
        return UpdateProfileResult.SUCCESS;
    }

    private String normalizeRequiredField(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeOptionalField(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeFullName(String value) {
        String normalized = normalizeRequiredField(value);
        if (normalized == null) {
            return null;
        }
        return normalized.replaceAll("\\s+", " ");
    }

    public long getUsuariosRegistradosCount() {
        return userRepository.countUsersWithoutAdminRole();
    }
}

