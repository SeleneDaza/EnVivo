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

    /**
     * Registra un nuevo usuario con rol CLIENTE.
     * @return true si el registro fue exitoso, false si el usuario ya existe o las contraseñas no coinciden.
     */
    public boolean registrar(RegisterDTO dto) {
        // Validar que las contraseñas coincidan
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return false;
        }

        // Validar que el usuario no exista
        if (userRepository.existsByEmail(dto.getNewUsername())) {
            return false;
        }

        // Obtener o crear el rol CLIENTE
        Role rolCliente = roleRepository.findByName("CLIENTE")
                .orElseGet(() -> {
                    Role nuevoRol = new Role();
                    nuevoRol.setName("CLIENTE");
                    return roleRepository.save(nuevoRol);
                });

        // Crear y guardar el usuario
        User usuario = new User();
        usuario.setEmail(dto.getNewUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        usuario.setRoles(Set.of(rolCliente));

        userRepository.save(usuario);
        return true;
    }

    /**
     * Retorna solo los usuarios que NO tienen el rol ADMIN.
     */
    public List<User> getClientUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .noneMatch(r -> r.getName().equalsIgnoreCase("ADMIN")))
                .collect(java.util.stream.Collectors.toList());
    }
}

