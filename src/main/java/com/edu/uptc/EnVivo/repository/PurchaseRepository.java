package com.edu.uptc.EnVivo.repository;

import com.edu.uptc.EnVivo.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}

