package com.bhargav.titantrade.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.naming.spi.DirStateFactory.Result;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bhargav.titantrade.common.exception.StockAlreadyExistsException;
import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.stock.dto.CreateStockRequest;
import com.bhargav.titantrade.stock.dto.StockResponse;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.enums.AssetType;
import com.bhargav.titantrade.stock.repository.StockRepository;
import com.bhargav.titantrade.wallet.enums.CurrencyType;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

	@Mock
	StockRepository stockRepository;

	@InjectMocks
	StockService stockService;

	@Test
	void saveStock_shouldCreateStock_whenTickerDoesNotExist() {

		CreateStockRequest request = new CreateStockRequest("AAPL", "Apple", BigDecimal.valueOf(200), "exchange",
				CurrencyType.USD, "Technology", AssetType.STOCK);
		LocalDateTime now = LocalDateTime.now();
		UUID stockId = UUID.randomUUID();
		Stock stock = new Stock(stockId, "AAPL", "Apple", BigDecimal.valueOf(200), now, "exchange", CurrencyType.USD,
				"Technology", AssetType.STOCK, false, now, now);

		when(stockRepository.existsByTickerIgnoreCase(request.getTicker().trim())).thenReturn(false);
		when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		ApiResponse response = stockService.saveStock(request);

		assertTrue(response.isSuccess());
		assertEquals("Stock saved successfully", response.getMessage());

		verify(stockRepository).save(argThat(savedStock -> savedStock.getTicker().equals(stock.getTicker())
				&& savedStock.getCompanyName().equals(stock.getCompanyName())
				&& savedStock.getAssetType().equals(stock.getAssetType())
				&& savedStock.getCurrency().equals(stock.getCurrency())
				&& savedStock.getLastKnownPrice().compareTo(stock.getLastKnownPrice()) == 0 && savedStock.isActive()));

	}

	@Test
	void saveStock_shouldThrowStockAlreadyExistsException_whenTickerAlreadyExists() {
		CreateStockRequest request = new CreateStockRequest("AAPL", "Apple", BigDecimal.valueOf(200), "exchange",
				CurrencyType.USD, "Technology", AssetType.STOCK);

		when(stockRepository.existsByTickerIgnoreCase(request.getTicker().trim())).thenReturn(true);

		assertThrows(StockAlreadyExistsException.class, () -> stockService.saveStock(request));

		verify(stockRepository, never()).save(any());
	}

	@Test
	void getStockById_shouldReturnStock_whenStockExists() {
		LocalDateTime now = LocalDateTime.now();
		UUID stockId = UUID.randomUUID();
		Stock stock = new Stock(stockId, "AAPL", "Apple", BigDecimal.valueOf(200), now, "exchange", CurrencyType.USD,
				"Technology", AssetType.STOCK, false, now, now);
		when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));

		ApiResponse response = stockService.getStockById(stockId);
		assertTrue(response.isSuccess());
		assertEquals("Stock retrieved successfully", response.getMessage());
		StockResponse result = (StockResponse) response.getData();
		assertEquals("Apple", result.getCompanyName());
		assertEquals("AAPL", result.getTicker());
		assertEquals(CurrencyType.USD, result.getCurrency());
		assertEquals(stockId, result.getId());
		assertEquals(0, BigDecimal.valueOf(200).compareTo(result.getLastKnownPrice()));

	}
	
	@Test
	void getStockById_shouldThrowStockNotFoundException_whenStockDoesNotExist() {
		UUID stockId = UUID.randomUUID();
		when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

		assertThrows(StockNotFoundException.class, () -> stockService.getStockById(stockId));

		verify(stockRepository, never()).save(any());

	}

}
