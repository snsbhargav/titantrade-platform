package com.bhargav.titantrade.trade.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.trade.entity.StockTransaction;
import com.bhargav.titantrade.trade.enums.TradeType;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {

	Page<StockTransaction> findByUserId(UUID userId, Pageable pageable);

	Page<StockTransaction> findByUserIdAndStockId(UUID userId, UUID stockId, Pageable pageable);

	Page<StockTransaction> findByUserIdAndTradeType(UUID userId, TradeType tradeType, Pageable pageable);

	Page<StockTransaction> findByUserIdAndStockIdAndTradeType(UUID userId, UUID stockId, TradeType tradeType,
			Pageable pageable);

}
