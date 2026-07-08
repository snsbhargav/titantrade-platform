package com.bhargav.titantrade.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.bhargav.titantrade.common.exception.StockAlreadyExistsException;
import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.stock.dto.CreateStockRequest;
import com.bhargav.titantrade.stock.dto.StockListResponse;
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

	@Test
	void getStockByTicker_shouldReturnStock_whenTickerExists() {
		LocalDateTime now = LocalDateTime.now();
		UUID stockId = UUID.randomUUID();
		Stock stock = new Stock(stockId, "AAPL", "Apple", BigDecimal.valueOf(200), now, "exchange", CurrencyType.USD,
				"Technology", AssetType.STOCK, true, now, now);

		when(stockRepository.findByTickerIgnoreCase("AAPL")).thenReturn(Optional.of(stock));
		ApiResponse response = stockService.getStockByTicker("AAPL");

		assertTrue(response.isSuccess());
		StockResponse data = (StockResponse) response.getData();
		assertEquals("Apple", data.getCompanyName());
		assertEquals("AAPL", data.getTicker());
		assertEquals(0, BigDecimal.valueOf(200).compareTo(data.getLastKnownPrice()));
		assertEquals(AssetType.STOCK, data.getAssetType());
		assertEquals(true, data.isActive());

	}

	@Test
	void getStockByTicker_shouldThrowStockNotFoundException_whenTickerNotExists() {
		
		when(stockRepository.findByTickerIgnoreCase("AAPL")).thenReturn(Optional.empty());
		
		assertThrows(StockNotFoundException.class, ()-> stockService.getStockByTicker("AAPL"));
		
	}

	@Test
	void updateStockPrice_shouldUpdatePrice_whenStockExists() {
		LocalDateTime now = LocalDateTime.now();
		UUID stockId = UUID.randomUUID();
		Stock stock = new Stock(stockId, "AAPL", "Apple", BigDecimal.valueOf(200), now, "exchange", CurrencyType.USD,
				"Technology", AssetType.STOCK, true, now, now);
		stock.setId(stockId);
		when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
		BigDecimal newPrice = BigDecimal.valueOf(222);
		ApiResponse response = stockService.updateStockPrice(stockId, newPrice);

		assertTrue(response.isSuccess());
		assertEquals("Stock price updated successfully", response.getMessage());

		verify(stockRepository).save(argThat(updatedStock -> updatedStock.getLastKnownPrice().compareTo(newPrice) == 0
				&& updatedStock.getLastPriceUpdatedAt() !=null));
	}
	
	@Test
	void updateStockPrice_shouldThrowStockNotFoundException_whenStockDoesNotExist() {
		UUID stockId = UUID.randomUUID();
		
		when(stockRepository.findById(stockId)).thenReturn(Optional.empty());
		
		assertThrows(StockNotFoundException.class, ()-> stockService.updateStockPrice(stockId, BigDecimal.valueOf(12)));
		
		verify(stockRepository, never()).save(any());
	}
	
	@Test
	void getAllStocks_shouldReturnPaginatedActiveStocks_whenNoSearchProvided() {
		LocalDateTime now = LocalDateTime.now();
		Stock stock1 = new Stock(UUID.randomUUID(), "AAPL", "Apple", BigDecimal.valueOf(200), now, "exchange", CurrencyType.USD,
				"Technology", AssetType.STOCK, true, now, now);
		Stock stock2 = new Stock(UUID.randomUUID(), "MSFT", "Microsoft", BigDecimal.valueOf(250), now, "exchange", CurrencyType.USD,
				"Technology", AssetType.STOCK, true, now, now);
		List<Stock> stocks = List.of(stock1, stock2);
		Pageable pageable = PageRequest.of(0, 10, Sort.by("ticker").ascending());
		Page<Stock> stockPage = new PageImpl<>(stocks, pageable, 2);
		when(stockRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(stockPage);
		
		ApiResponse response = stockService.getAllStocks(0, 10, null);
		assertTrue(response.isSuccess());
		StockListResponse result = (StockListResponse) response.getData();
		assertEquals(0, result.getPage());
		assertEquals(10, result.getSize());
		assertEquals(2, result.getStocks().size());
		assertEquals(2, result.getTotalElements());
		assertEquals(1, result.getTotalPages());
	}
	
	@Test
	void getAllStocks_shouldCapSizeToHundred_whenSizeGreaterThanHundred() {
		LocalDateTime now = LocalDateTime.now();
		Stock stock1 = new Stock(UUID.randomUUID(), "AAPL", "Apple", BigDecimal.valueOf(200), now, "exchange", CurrencyType.USD,
				"Technology", AssetType.STOCK, true, now, now);
		Stock stock2 = new Stock(UUID.randomUUID(), "MSFT", "Microsoft", BigDecimal.valueOf(250), now, "exchange", CurrencyType.USD,
				"Technology", AssetType.STOCK, true, now, now);
		List<Stock> stocks = List.of(stock1, stock2);
		Pageable pageable = PageRequest.of(0, 100, Sort.by("ticker").ascending());
		Page<Stock> stockPage = new PageImpl<>(stocks, pageable, 2);
		
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		when(stockRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(stockPage);
		
		ApiResponse response = stockService.getAllStocks(0, 1000, null);

		verify(stockRepository).findAll(any(Specification.class), pageableCaptor.capture());

		Pageable usedPageable = pageableCaptor.getValue();

		assertEquals(100, usedPageable.getPageSize());
		assertEquals(0, usedPageable.getPageNumber());
		assertTrue(response.isSuccess());
		assertEquals(100, ((StockListResponse)response.getData()).getSize());
	}

}
