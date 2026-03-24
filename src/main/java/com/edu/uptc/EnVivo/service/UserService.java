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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean registerUser(RegisterDTO dto) {
        if (!arePasswordsMatching(dto)) {
            return false;
        }

        if (userExists(dto.getNewUsername())) {
            return false;
        }

        Role rolCliente = getOrCreateClientRole();
        saveNewUser(dto, rolCliente);
        return true;
    }

    private boolean arePasswordsMatching(RegisterDTO dto) {
        return dto.getNewPassword().equals(dto.getConfirmPassword());
    }

    private boolean userExists(String username) {
        return userRepository.existsByEmail(username);
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

    private void saveNewUser(RegisterDTO dto, Role role) {
        User user = new User();
        user.setEmail(dto.getNewUsername());
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }
    
    public List<User> getClientUsers() {
        return userRepository.findUsersWithoutAdminRole();
    }
}
