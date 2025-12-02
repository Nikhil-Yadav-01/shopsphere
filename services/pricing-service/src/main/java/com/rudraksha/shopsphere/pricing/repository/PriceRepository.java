package com.rudraksha.shopsphere.pricing.repository;

import com.rudraksha.shopsphere.pricing.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    Optional<Price> findByProductId(String productId);
}
