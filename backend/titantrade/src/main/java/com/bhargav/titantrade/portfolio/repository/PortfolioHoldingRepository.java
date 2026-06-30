package com.bhargav.titantrade.portfolio.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.portfolio.entity.PortfolioHolding;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, UUID>{
	
	List<PortfolioHolding> findByUserId(UUID userId);
	
	Optional<PortfolioHolding> findByUserIdAndStockId(UUID userId, UUID stockId);
	
	List<PortfolioHolding> findByUserIdAndQuantityGreaterThan(UUID userId, BigDecimal quantity);

}
