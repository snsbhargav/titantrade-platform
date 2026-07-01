package com.bhargav.titantrade.trade.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.trade.entity.StockTransaction;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID>{

	List<StockTransaction> findByUserId(UUID userId);
	
	List<StockTransaction> findByUserIdAndStockId(UUID userId, UUID stockId);

	List<StockTransaction> findByUserIdOrderByExecutedAtDesc(UUID id);

	List<StockTransaction> findByUserIdAndStockIdOrderByExecutedAtDesc(UUID id, UUID stockId);
	
}
