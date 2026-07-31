package com.bhargav.titantrade.trade.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bhargav.titantrade.common.constants.DecimalConstants;
import com.bhargav.titantrade.common.exception.InsufficientFundsException;
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
import com.bhargav.titantrade.trade.dto.OrderResponse;
import com.bhargav.titantrade.trade.dto.SellStockRequest;
import com.bhargav.titantrade.trade.dto.StockTransactionResponse;
import com.bhargav.titantrade.trade.dto.TradeHistoryResponse;
import com.bhargav.titantrade.trade.entity.Order;
import com.bhargav.titantrade.trade.entity.StockTransaction;
import com.bhargav.titantrade.trade.enums.TradeStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import com.bhargav.titantrade.trade.repository.OrderRepository;
import com.bhargav.titantrade.trade.repository.StockTransactionRepository;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.wallet.service.WalletService;

@Service
public class TradeService {

	private final StockTransactionRepository stockTransactionRepository;
	private final CurrentUserService currentUserService;
	private final StockService stockService;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final StockRepository stockRepository;
	private final WalletService walletService;
	private final OrderRepository orderRepository;

	public TradeService(StockTransactionRepository stockTransactionRepository, CurrentUserService currentUserService,
			StockService stockService, PortfolioHoldingRepository portfolioHoldingRepository,
			StockRepository stockRepository, WalletService walletService, OrderRepository orderRepository) {
		this.stockTransactionRepository = stockTransactionRepository;
		this.currentUserService = currentUserService;
		this.stockService = stockService;
		this.portfolioHoldingRepository = portfolioHoldingRepository;
		this.stockRepository = stockRepository;
		this.walletService = walletService;
		this.orderRepository = orderRepository;
	}

	@Transactional(readOnly = true)
	public ApiResponse getMyTradeHistory(UUID stockId, TradeType tradeType, int page, int size) {
		User user = currentUserService.getCurrentUser();
		List<StockTransactionResponse> response = new ArrayList<>();
		Page<StockTransaction> tradeHistory;
		if (size > 100)
			size = 100;
		if (size <= 0)
			size = 10;
		if (page < 0)
			page = 0;
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
		UUID idempotencyKey = buyStockRequest.getIdempotencyKey();
		User user = currentUserService.getCurrentUser();
		Optional<Order> existingOrder = orderRepository.findByUserIdAndIdempotencyKey(user.getId(), idempotencyKey);
		if (existingOrder.isPresent())
			return new ApiResponse(true, "Order already exists", OrderResponse.toDto(existingOrder.get()));
		Stock stock = stockRepository.findById(buyStockRequest.getStockId())
				.orElseThrow(() -> new StockNotFoundException("Stock not found"));
		BigDecimal quantity = buyStockRequest.getQuantity().setScale(DecimalConstants.QUANTITY_SCALE,
				DecimalConstants.ROUNDING_MODE);
		Order order = Order.createPendingOrder(user, stock, quantity, idempotencyKey, TradeType.BUY);
		order = orderRepository.save(order);
		// If stock is inactive don't trade
		if (!stock.isActive()) {
			// mark order as rejected
			order.markRejected("Stock is inactive and cannot be traded");
			orderRepository.save(order);
//			throw new InactiveStockException("Stock is inactive and cannot be traded");
			return new ApiResponse(false, "Stock is inactive and cannot be traded", OrderResponse.toDto(order));
		}
		BigDecimal executionPrice = stock.getLastKnownPrice().setScale(DecimalConstants.PRICE_SCALE,
				DecimalConstants.ROUNDING_MODE);

		BigDecimal totalBuyPrice = executionPrice.multiply(quantity).setScale(DecimalConstants.PRICE_SCALE,
				DecimalConstants.ROUNDING_MODE);
		BigDecimal walletDebitAmount = totalBuyPrice.setScale(DecimalConstants.MONEY_SCALE,
				DecimalConstants.ROUNDING_MODE);
		// Update Wallet balance & wallet transaction
		try {
			walletService.debitCurrentUserWallet(walletDebitAmount);
		} catch (InsufficientFundsException ex) {
			order.markRejected("Insufficient funds");
			orderRepository.save(order);

			return new ApiResponse(false, "Insufficient funds", OrderResponse.toDto(order));
		}

		PortfolioHolding portfolioHolding = portfolioHoldingRepository
				.findByUserIdAndStockId(user.getId(), buyStockRequest.getStockId()).orElse(null);

//		LocalDateTime now = LocalDateTime.now();
		// If portfolio doesn't exists
		if (portfolioHolding == null) {
			// Update Portfolio
			portfolioHolding = new PortfolioHolding(user, stock, executionPrice, quantity);
		} else {
			BigDecimal oldValue = portfolioHolding.getQuantity().multiply(portfolioHolding.getAverageBuyPrice())
					.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE);
			BigDecimal newValue = totalBuyPrice;
			BigDecimal combinedQuantity = portfolioHolding.getQuantity().add(quantity)
					.setScale(DecimalConstants.QUANTITY_SCALE, DecimalConstants.ROUNDING_MODE);
			BigDecimal averageBuyPrice = oldValue.add(newValue).divide(combinedQuantity, DecimalConstants.PRICE_SCALE,
					DecimalConstants.ROUNDING_MODE);

			portfolioHolding.setAverageBuyPrice(averageBuyPrice);
			portfolioHolding.setQuantity(combinedQuantity);
		}
		portfolioHoldingRepository.save(portfolioHolding);

		// UpdateStock transaction
		recordStockTransaction(user, stock, executionPrice, quantity, totalBuyPrice, TradeStatus.SUCCESS,
				TradeType.BUY);

		// Mark order as executed
		order.markExecuted(executionPrice, totalBuyPrice);
		orderRepository.save(order);
		return new ApiResponse(true, "Buy order executed successfully", OrderResponse.toDto(order));
	}

	@Transactional
	public ApiResponse sellStock(SellStockRequest sellStockRequest) {
		User user = currentUserService.getCurrentUser();
		UUID idempotencyKey = sellStockRequest.getIdempotencyKey();
		Optional<Order> existingOrder = orderRepository.findByUserIdAndIdempotencyKey(user.getId(), idempotencyKey);
		if (existingOrder.isPresent()) {
			return new ApiResponse(true, "Order already exists", OrderResponse.toDto(existingOrder.get()));
		}
		Stock stock = stockRepository.findById(sellStockRequest.getStockId())
				.orElseThrow(() -> new StockNotFoundException("Stock not found"));
		BigDecimal sellQuantity = sellStockRequest.getQuantity().setScale(DecimalConstants.QUANTITY_SCALE,
				DecimalConstants.ROUNDING_MODE);
		// Create Pending order
		Order order = Order.createPendingOrder(user, stock, sellQuantity, idempotencyKey, TradeType.SELL);
		order = orderRepository.save(order);
		Optional<PortfolioHolding> holdingOptional = portfolioHoldingRepository.findByUserIdAndStockId(user.getId(),
				stock.getId());
		if (holdingOptional.isEmpty()) {

			order.markRejected("Portfolio not found");
			orderRepository.save(order);
			return new ApiResponse(false, "Portfolio not found", OrderResponse.toDto(order));
		}
		PortfolioHolding portfolioHolding = holdingOptional.get();
		BigDecimal executionPrice = stock.getLastKnownPrice().setScale(DecimalConstants.PRICE_SCALE,
				DecimalConstants.ROUNDING_MODE);

		// update quantity in portfolio
		if (portfolioHolding.getQuantity().compareTo(sellQuantity) < 0) {
			order.markRejected("Insufficient holdings");
			orderRepository.save(order);
			return new ApiResponse(false, "Insufficient holdings", OrderResponse.toDto(order));
//			throw new InsufficientHoldingQuantityException("Insufficient holdings");
		}
		portfolioHolding.setQuantity(portfolioHolding.getQuantity().subtract(sellQuantity)
				.setScale(DecimalConstants.QUANTITY_SCALE, DecimalConstants.ROUNDING_MODE));
		portfolioHoldingRepository.save(portfolioHolding);

		// update wallet
		BigDecimal totalSellAmount = executionPrice.multiply(sellQuantity).setScale(DecimalConstants.PRICE_SCALE,
				DecimalConstants.ROUNDING_MODE);

		BigDecimal walletCreditAmount = totalSellAmount.setScale(DecimalConstants.MONEY_SCALE,
				DecimalConstants.ROUNDING_MODE);
		walletService.creditCurrentUserWallet(walletCreditAmount);

		// add portfolio transaction
		recordStockTransaction(user, stock, executionPrice, sellQuantity, totalSellAmount, TradeStatus.SUCCESS,
				TradeType.SELL);

		order.markExecuted(executionPrice, totalSellAmount);
		orderRepository.save(order);

		return new ApiResponse(true, "Sell order executed successfully", OrderResponse.toDto(order));
	}

	private void recordStockTransaction(User user, Stock stock, BigDecimal pricePerShare, BigDecimal quantity,
			BigDecimal totalAmount, TradeStatus tradeStatus, TradeType tradeType) {
		StockTransaction stockTransaction = new StockTransaction();
		stockTransaction.setUser(user);
		stockTransaction.setStock(stock);
		stockTransaction
				.setPricePerShare(pricePerShare.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE));
		stockTransaction
				.setQuantity(quantity.setScale(DecimalConstants.QUANTITY_SCALE, DecimalConstants.ROUNDING_MODE));
		stockTransaction
				.setTotalAmount(totalAmount.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE));
		stockTransaction.setTradeStatus(tradeStatus);
		stockTransaction.setTradeType(tradeType);
		stockTransactionRepository.save(stockTransaction);
	}

}
