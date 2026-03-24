package com.edu.uptc.EnVivo.repository;

import com.edu.uptc.EnVivo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE NOT EXISTS (SELECT r FROM u.roles r WHERE UPPER(r.name) = 'ADMIN')")
    List<User> findUsersWithoutAdminRole();
}