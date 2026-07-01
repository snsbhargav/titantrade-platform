package com.bhargav.titantrade.trade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.service.StockService;
import com.bhargav.titantrade.trade.dto.StockTransactionResponse;
import com.bhargav.titantrade.trade.entity.StockTransaction;
import com.bhargav.titantrade.trade.enums.TradeStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import com.bhargav.titantrade.trade.repository.StockTransactionRepository;
import com.bhargav.titantrade.user.entity.User;

@Service
public class StockTransactionService {

	private final StockTransactionRepository stockTransactionRepository;
	private final CurrentUserService currentUserService;
	private final StockService stockService;

	public StockTransactionService(StockTransactionRepository stockTransactionRepository,
			CurrentUserService currentUserService, StockService stockService) {
		this.stockTransactionRepository = stockTransactionRepository;
		this.currentUserService = currentUserService;
		this.stockService = stockService;
	}

	public void recordStockTransaction(User user, Stock stock, BigDecimal pricePerShare, BigDecimal quantity,
			BigDecimal totalAmount, TradeStatus tradeStatus, TradeType tradeType, LocalDateTime createdOn,
			LocalDateTime updatedOn, LocalDateTime executedAt) {
		StockTransaction stockTransaction = new StockTransaction();
		stockTransaction.setUser(user);
		stockTransaction.setStock(stock);
		stockTransaction.setPricePerShare(pricePerShare);
		stockTransaction.setQuantity(quantity);
		stockTransaction.setTotalAmount(totalAmount);
		stockTransaction.setTradeStatus(tradeStatus);
		stockTransaction.setTradeType(tradeType);
		stockTransaction.setCreatedOn(createdOn);
		stockTransaction.setUpdatedOn(updatedOn);
		stockTransaction.setExecutedAt(executedAt);
		stockTransactionRepository.save(stockTransaction);
	}

	public ApiResponse getMyTradeHistory(UUID stockId) {
		User user = currentUserService.getCurrentUser();
		List<StockTransactionResponse> response = new ArrayList<>();
		List<StockTransaction> tradeHistory;
		if (stockId == null)
			tradeHistory = stockTransactionRepository.findByUserIdOrderByExecutedAtDesc(user.getId());
		else {
			stockService.getStockById(stockId);
			tradeHistory = stockTransactionRepository.findByUserIdAndStockIdOrderByExecutedAtDesc(user.getId(),
					stockId);
		}
		for (StockTransaction transaction : tradeHistory) {
			response.add(StockTransactionResponse.toDto(transaction));
		}
		return new ApiResponse(true, "Stock transactions retrieved successfully", response);
	}

}
