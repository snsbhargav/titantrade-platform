package com.bhargav.titantrade.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.portfolio.dto.BuyStockRequest;
import com.bhargav.titantrade.portfolio.entity.PortfolioHolding;
import com.bhargav.titantrade.portfolio.repository.PortfolioHoldingRepository;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.repository.StockRepository;
import com.bhargav.titantrade.trade.enums.TradeStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import com.bhargav.titantrade.trade.service.StockTransactionService;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.wallet.dto.WalletAmountRequest;
import com.bhargav.titantrade.wallet.service.WalletService;

import jakarta.transaction.Transactional;

@Service
public class PortfolioHoldingService {

	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final StockRepository stockRepository;
	private final CurrentUserService currentUserService;
	private final WalletService walletService;
	private final StockTransactionService stockTransactionService;

	public PortfolioHoldingService(PortfolioHoldingRepository portfolioHoldingRepository,
			StockRepository stockRepository, CurrentUserService currentUserService, WalletService walletService,
			StockTransactionService stockTransactionService) {
		this.portfolioHoldingRepository = portfolioHoldingRepository;
		this.stockRepository = stockRepository;
		this.currentUserService = currentUserService;
		this.walletService = walletService;
		this.stockTransactionService = stockTransactionService;
	}

	@Transactional
	public ResponseEntity<ApiResponse> buyStock(BuyStockRequest buyStockRequest) {
		Stock stock = stockRepository.findById(buyStockRequest.getStockId())
				.orElseThrow(() -> new StockNotFoundException("Stock not found"));
		User user = currentUserService.getCurrentUser();
		BigDecimal totalBuyPrice = stock.getLastKnownPrice().multiply(buyStockRequest.getQuantity());
		// Update Wallet balance & wallet transaction
		walletService.withdrawAmount(new WalletAmountRequest(totalBuyPrice));

		PortfolioHolding portfolioHolding = portfolioHoldingRepository
				.findByUserIdAndStockId(user.getId(), buyStockRequest.getStockId()).orElse(null);

		LocalDateTime now = LocalDateTime.now();
		// If portfolio doesn't exists
		if (portfolioHolding == null) {
			// Update Portfolio
			portfolioHolding = new PortfolioHolding(null, user, stock, stock.getLastKnownPrice(), buyStockRequest.getQuantity(),
					now, now);
		} else {
			BigDecimal oldValue = portfolioHolding.getQuantity().multiply(portfolioHolding.getAverageBuyPrice());
			BigDecimal newValue = stock.getLastKnownPrice().multiply(buyStockRequest.getQuantity());
			BigDecimal combinedQuantity = portfolioHolding.getQuantity().add(buyStockRequest.getQuantity());
			BigDecimal averageBuyPrice = oldValue.add(newValue).divide(combinedQuantity, 4, RoundingMode.HALF_UP);

			portfolioHolding.setAverageBuyPrice(averageBuyPrice);
			portfolioHolding.setQuantity(portfolioHolding.getQuantity().add(buyStockRequest.getQuantity()));
			portfolioHolding.setUpdatedOn(now);
		}
		portfolioHoldingRepository.save(portfolioHolding);

		// UpdateStock transaction
		stockTransactionService.recordStockTransaction(user, stock, stock.getLastKnownPrice(),
				buyStockRequest.getQuantity(),
				stock.getLastKnownPrice().multiply(buyStockRequest.getQuantity()), TradeStatus.SUCCESS,
				TradeType.BUY, now, now, now);

		return new ResponseEntity<ApiResponse>(new ApiResponse(true, "Stock bought successfully", portfolioHolding),
				HttpStatus.OK);
	}

}
