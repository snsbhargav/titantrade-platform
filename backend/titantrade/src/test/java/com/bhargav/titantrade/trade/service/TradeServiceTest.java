package com.bhargav.titantrade.trade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bhargav.titantrade.common.exception.InactiveStockException;
import com.bhargav.titantrade.common.exception.InsufficientFundsException;
import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.portfolio.entity.PortfolioHolding;
import com.bhargav.titantrade.portfolio.repository.PortfolioHoldingRepository;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.enums.AssetType;
import com.bhargav.titantrade.stock.repository.StockRepository;
import com.bhargav.titantrade.stock.service.StockService;
import com.bhargav.titantrade.trade.dto.BuyStockRequest;
import com.bhargav.titantrade.trade.enums.TradeStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import com.bhargav.titantrade.trade.repository.StockTransactionRepository;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.wallet.entity.Wallet;
import com.bhargav.titantrade.wallet.enums.CurrencyType;
import com.bhargav.titantrade.wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private StockService stockService;

	@Mock
	private WalletService walletService;

	@Mock
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Mock
	private StockTransactionRepository stockTransactionRepository;

	@Mock
	private StockRepository stockRepository;

	@InjectMocks
	private TradeService tradeService;
	
	private UUID userId, stockId;

	@BeforeEach
	void setUp() {
		stockId = UUID.randomUUID();
		userId = UUID.randomUUID();
	}
	
	private Stock createdTempStock(BigDecimal price) {
		Stock stock = new Stock();
		stock.setId(stockId);
		stock.setCompanyName("Apple");
		stock.setTicker("AAPL");
		stock.setAssetType(AssetType.STOCK);
		stock.setCurrency(CurrencyType.USD);
		stock.setLastKnownPrice(price);
		stock.setActive(true);
		
		return stock;
	}

	@Test
	void buyStock_shouldCreateNewHolding_whenUserDoesNotOwnStockYet() {

		User user = new User();
		user.setId(userId);
		Stock stock = createdTempStock(BigDecimal.valueOf(190));
		

		BuyStockRequest buyStockRequest = new BuyStockRequest(stockId, BigDecimal.ONE);

		when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
		when(currentUserService.getCurrentUser()).thenReturn(user);
		// Check wallet balance debited successfully
		when(portfolioHoldingRepository.findByUserIdAndStockId(userId, buyStockRequest.getStockId()))
				.thenReturn(Optional.empty());

		// Make call
		ApiResponse response = tradeService.buyStock(buyStockRequest);

		// Check
		assertTrue(response.isSuccess());
		assertEquals("Stock bought successfully", response.getMessage());

		verify(walletService)
				.withdrawAmount(argThat(wallet -> wallet.getAmount().compareTo(BigDecimal.valueOf(190)) == 0));

		verify(portfolioHoldingRepository)
				.save(argThat(holding -> holding.getUser().equals(user) && holding.getStock().equals(stock)
						&& holding.getQuantity().compareTo(buyStockRequest.getQuantity()) == 0
						&& holding.getAverageBuyPrice().compareTo(stock.getLastKnownPrice()) == 0));

		verify(stockTransactionRepository).save(argThat(transaction -> transaction.getStock().equals(stock)
				&& transaction.getPricePerShare().compareTo(stock.getLastKnownPrice()) == 0
				&& transaction.getUser().equals(user)
				&& transaction.getQuantity().compareTo(buyStockRequest.getQuantity()) == 0
				&& transaction.getTradeStatus().equals(TradeStatus.SUCCESS)
				&& transaction.getTradeType().equals(TradeType.BUY)
				&& transaction.getTotalAmount().compareTo(BigDecimal.valueOf(190)) == 0));

	}

	@Test
	void buyStock_shouldUpdateExistingHoldingAndRecalculateAveragePrice_whenUserAlreadyOwnsStock() {

		UUID holdingId = UUID.randomUUID();

		User user = new User();
		user.setId(userId);

		Stock stock = createdTempStock(BigDecimal.valueOf(200));

		PortfolioHolding holding = new PortfolioHolding();
		holding.setId(holdingId);
		holding.setQuantity(BigDecimal.valueOf(2));
		holding.setStock(stock);
		holding.setUser(user);
		holding.setAverageBuyPrice(BigDecimal.valueOf(100));

		BuyStockRequest buyStockRequest = new BuyStockRequest(stockId, BigDecimal.valueOf(2));

		when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
		when(currentUserService.getCurrentUser()).thenReturn(user);
		// Check wallet balance debited successfully
		when(portfolioHoldingRepository.findByUserIdAndStockId(userId, buyStockRequest.getStockId()))
				.thenReturn(Optional.of(holding));

		// Make call
		ApiResponse response = tradeService.buyStock(buyStockRequest);
		
		assertTrue(response.isSuccess());
		assertEquals("Stock bought successfully", response.getMessage());
		
		verify(walletService)
		.withdrawAmount(argThat(wallet -> wallet.getAmount().compareTo(BigDecimal.valueOf(400)) == 0));


		verify(portfolioHoldingRepository).save(argThat(savedHolding -> savedHolding.getId().equals(holding.getId())
				&& savedHolding.getStock().equals(stock) && savedHolding.getUser().equals(user)
				&& savedHolding.getQuantity().compareTo(BigDecimal.valueOf(4)) == 0
				&& savedHolding.getAverageBuyPrice().compareTo(BigDecimal.valueOf(150)) == 0));
		
		verify(stockTransactionRepository).save(argThat(transaction -> transaction.getStock().equals(stock)
				&& transaction.getPricePerShare().compareTo(stock.getLastKnownPrice()) == 0
				&& transaction.getUser().equals(user)
				&& transaction.getQuantity().compareTo(buyStockRequest.getQuantity()) == 0
				&& transaction.getTradeStatus().equals(TradeStatus.SUCCESS)
				&& transaction.getTradeType().equals(TradeType.BUY)
				&& transaction.getTotalAmount().compareTo(BigDecimal.valueOf(400)) == 0));
	}
	
	@Test
	void buyStock_shouldThrowInactiveStockException_whenStockIsInactive() {
		Stock stock = createdTempStock(BigDecimal.valueOf(200));
		
		when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
		stock.setActive(false);
		BuyStockRequest buyStockRequest = new BuyStockRequest(stockId, BigDecimal.valueOf(2));

		InactiveStockException execption = assertThrows(InactiveStockException.class, ()->tradeService.buyStock(buyStockRequest));
		
		assertEquals("Stock is inactive and cannot be traded", execption.getMessage());
		
		verify(walletService, never()).withdrawAmount(any());
		verify(portfolioHoldingRepository, never()).save(any());
		verify(stockTransactionRepository, never()).save(any());

	}
	
	@Test
	void buyStock_shouldThrowStockNotFoundException_whenStockDoesNotExist() {
		
		BuyStockRequest request = new BuyStockRequest(stockId, BigDecimal.ONE);
		
		when(stockRepository.findById(stockId)).thenReturn(Optional.empty());
		
		assertThrows(StockNotFoundException.class, ()-> tradeService.buyStock(request));
		verify(walletService, never()).withdrawAmount(any());
		verify(portfolioHoldingRepository, never()).save(any());
		verify(stockTransactionRepository, never()).save(any());
	}
	
	@Test
	void buyStock_shouldNotSaveHoldingOrTransaction_whenWalletWithdrawFails() {
		Stock stock = createdTempStock(BigDecimal.valueOf(200));
		User user = new User();
		BuyStockRequest request = new BuyStockRequest(stockId, BigDecimal.valueOf(2));
		
		when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
		when(currentUserService.getCurrentUser()).thenReturn(user);
		doThrow(new InsufficientFundsException("Insufficient funds")).when(walletService).withdrawAmount(any());
		
		assertThrows(InsufficientFundsException.class, ()-> tradeService.buyStock(request));
		
		
		verify(portfolioHoldingRepository, never()).save(any());
		verify(stockTransactionRepository, never()).save(any());
		
	}
	
	
	
	
	
	
}