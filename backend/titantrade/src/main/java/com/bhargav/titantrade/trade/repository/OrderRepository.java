package com.bhargav.titantrade.trade.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.trade.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order>{

	boolean existsByIdempotencyKey(UUID idempotencyKey);
	
	Optional<Order> findByUserIdAndIdempotencyKey(UUID userId, UUID idempotencyKey);
	
	Page<Order> findByUserId(UUID id, Pageable pageable);

}
