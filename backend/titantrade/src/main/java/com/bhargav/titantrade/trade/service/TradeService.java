package com.bhargav.titantrade.trade.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.exception.InactiveStockException;
import com.bhargav.titantrade.common.exception.InsufficientHoldingQuantityException;
import com.bhargav.titantrade.common.exception.PortfolioHoldingNotFoundException;
import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.portfolio.dto.PortfolioHoldingResponse;
import com.bhargav.titantrade.portfolio.entity.PortfolioHolding;
import com.bhargav.titantrade.portfolio.repository.PortfolioHoldingRepository;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.repository.StockRepository;
import com.bhargav.titantrade.stock.service.StockService;
import com.bhargav.titantrade.trade.dto.BuyStockRequest;
import com.bhargav.titantrade.trade.dto.SellStockRequest;
import com.bhargav.titantrade.trade.dto.StockTransactionResponse;
import com.bhargav.titantrade.trade.dto.TradeHistoryResponse;
import com.bhargav.titantrade.trade.entity.StockTransaction;
import com.bhargav.titantrade.trade.enums.TradeStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import com.bhargav.titantrade.trade.repository.StockTransactionRepository;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.wallet.dto.WalletAmountRequest;
import com.bhargav.titantrade.wallet.service.WalletService;

import jakarta.transaction.Transactional;

@Service
public class TradeService {

	private final StockTransactionRepository stockTransactionRepository;
	private final CurrentUserService currentUserService;
	private final StockService stockService;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final StockRepository stockRepository;
	private final WalletService walletService;

	public TradeService(StockTransactionRepository stockTransactionRepository, CurrentUserService currentUserService,
			StockService stockService, PortfolioHoldingRepository portfolioHoldingRepository,
			StockRepository stockRepository, WalletService walletService) {
		this.stockTransactionRepository = stockTransactionRepository;
		this.currentUserService = currentUserService;
		this.stockService = stockService;
		this.portfolioHoldingRepository = portfolioHoldingRepository;
		this.stockRepository = stockRepository;
		this.walletService = walletService;
	}

	public void recordStockTransaction(User user, Stock stock, BigDecimal pricePerShare, BigDecimal quantity,
			BigDecimal totalAmount, TradeStatus tradeStatus, TradeType tradeType) {
		StockTransaction stockTransaction = new StockTransaction();
		stockTransaction.setUser(user);
		stockTransaction.setStock(stock);
		stockTransaction.setPricePerShare(pricePerShare);
		stockTransaction.setQuantity(quantity);
		stockTransaction.setTotalAmount(totalAmount);
		stockTransaction.setTradeStatus(tradeStatus);
		stockTransaction.setTradeType(tradeType);
//		stockTransaction.setExecutedAt(executedAt);
		stockTransactionRepository.save(stockTransaction);
	}

	public ApiResponse getMyTradeHistory(UUID stockId, TradeType tradeType, int page, int size) {
		User user = currentUserService.getCurrentUser();
		List<StockTransactionResponse> response = new ArrayList<>();
		Page<StockTransaction> tradeHistory;
		if (size > 100) size = 100;
		if(size<0) size = 10;
		if(page<0) page=0;
		Pageable pageable = PageRequest.of(page, size, Sort.by("executedAt").descending());
		if (stockId == null && tradeType == null) {
			tradeHistory = stockTransactionRepository.findByUserId(user.getId(), pageable);
		} else if (stockId == null && tradeType != null) {
			tradeHistory = stockTransactionRepository.findByUserIdAndTradeType(user.getId(), tradeType, pageable);
		} else if (stockId != null && tradeType == null) {
			stockService.getStockById(stockId);
			tradeHistory = stockTransactionRepository.findByUserIdAndStockId(user.getId(), stockId, pageable);
		} else {
			stockService.getStockById(stockId);
			tradeHistory = stockTransactionRepository.findByUserIdAndStockIdAndTradeType(user.getId(), stockId,
					tradeType, pageable);
		}

		for (StockTransaction transaction : tradeHistory.getContent()) {
			response.add(StockTransactionResponse.toDto(transaction));
		}
		TradeHistoryResponse tradeHistoryResponse = new TradeHistoryResponse(response, tradeHistory.getNumber(),
				tradeHistory.getSize(), tradeHistory.getTotalElements(), tradeHistory.getTotalPages(),
				tradeHistory.isLast());
		return new ApiResponse(true, "Stock transactions retrieved successfully", tradeHistoryResponse);
	}

	@Transactional
	public ApiResponse buyStock(BuyStockRequest buyStockRequest) {
		Stock stock = stockRepository.findById(buyStockRequest.getStockId())
				.orElseThrow(() -> new StockNotFoundException("Stock not found"));
		// If stock is inactive don't trade
		if (!stock.isActive())
			throw new InactiveStockException("Stock is inactive and cannot be traded");
		User user = currentUserService.getCurrentUser();
		BigDecimal executionPrice = stock.getLastKnownPrice();
		BigDecimal totalBuyPrice = executionPrice.multiply(buyStockRequest.getQuantity());
		// Update Wallet balance & wallet transaction
		walletService.withdrawAmount(new WalletAmountRequest(totalBuyPrice));

		PortfolioHolding portfolioHolding = portfolioHoldingRepository
				.findByUserIdAndStockId(user.getId(), buyStockRequest.getStockId()).orElse(null);

//		LocalDateTime now = LocalDateTime.now();
		// If portfolio doesn't exists
		if (portfolioHolding == null) {
			// Update Portfolio
			portfolioHolding = new PortfolioHolding(user, stock, executionPrice, buyStockRequest.getQuantity());
		} else {
			BigDecimal oldValue = portfolioHolding.getQuantity().multiply(portfolioHolding.getAverageBuyPrice());
			BigDecimal newValue = executionPrice.multiply(buyStockRequest.getQuantity());
			BigDecimal combinedQuantity = portfolioHolding.getQuantity().add(buyStockRequest.getQuantity());
			BigDecimal averageBuyPrice = oldValue.add(newValue).divide(combinedQuantity, 4, RoundingMode.HALF_UP);

			portfolioHolding.setAverageBuyPrice(averageBuyPrice);
			portfolioHolding.setQuantity(portfolioHolding.getQuantity().add(buyStockRequest.getQuantity()));
//			portfolioHolding.setUpdatedOn(now);
		}
		portfolioHoldingRepository.save(portfolioHolding);

		// UpdateStock transaction
		recordStockTransaction(user, stock, executionPrice, buyStockRequest.getQuantity(),
				executionPrice.multiply(buyStockRequest.getQuantity()), TradeStatus.SUCCESS, TradeType.BUY);

		return new ApiResponse(true, "Stock bought successfully", PortfolioHoldingResponse.toDto(portfolioHolding));
	}

	@Transactional
	public ApiResponse sellStock(SellStockRequest sellStockRequest) {
		Stock stock = stockRepository.findById(sellStockRequest.getStockId())
				.orElseThrow(() -> new StockNotFoundException("Stock not found"));
		User user = currentUserService.getCurrentUser();
		PortfolioHolding portfolioHolding = portfolioHoldingRepository
				.findByUserIdAndStockId(user.getId(), stock.getId())
				.orElseThrow(() -> new PortfolioHoldingNotFoundException("Portfolio not found"));
//		LocalDateTime now = LocalDateTime.now();
		BigDecimal sellQuantity = sellStockRequest.getQuantity();
		BigDecimal executionPrice = stock.getLastKnownPrice();

		// update quantity in portfolio
		if (portfolioHolding.getQuantity().compareTo(sellQuantity) < 0) {
			throw new InsufficientHoldingQuantityException("Insufficient holdings");
		}
		portfolioHolding.setQuantity(portfolioHolding.getQuantity().subtract(sellQuantity));
//		portfolioHolding.setUpdatedOn(now);
		portfolioHoldingRepository.save(portfolioHolding);

		// update wallet
		walletService.depositAmount(new WalletAmountRequest(sellQuantity.multiply(executionPrice)));

		// add portfolio transaction
		recordStockTransaction(user, stock, executionPrice, sellQuantity, executionPrice.multiply(sellQuantity),
				TradeStatus.SUCCESS, TradeType.SELL);

		return new ApiResponse(true, "Stock sold successfully", PortfolioHoldingResponse.toDto(portfolioHolding));
	}

}
