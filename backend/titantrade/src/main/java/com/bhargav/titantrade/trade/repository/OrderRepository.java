package com.bhargav.titantrade.trade.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.trade.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>{

	boolean existsByIdempotencyKey(UUID idempotencyKey);
	
	Optional<Order> findByUserIdAndIdempotencyKey(UUID userId, UUID idempotencyKey);

}
