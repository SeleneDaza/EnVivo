package com.edu.uptc.EnVivo.repository;

import com.edu.uptc.EnVivo.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
	List<Purchase> findByUserIdOrderByPurchaseDateDesc(Long userId);
}

