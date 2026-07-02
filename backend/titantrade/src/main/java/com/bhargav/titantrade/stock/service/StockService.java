package com.bhargav.titantrade.stock.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.exception.StockAlreadyExistsException;
import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.stock.dto.CreateStockRequest;
import com.bhargav.titantrade.stock.dto.StockResponse;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.repository.StockRepository;

@Service
public class StockService {

	private final StockRepository stockRepository;

	public StockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	public ApiResponse saveStock(CreateStockRequest stockDto) {
		Stock stock = CreateStockRequest.toEntity(stockDto);
		if(stockRepository.existsByTickerIgnoreCase(stock.getTicker().trim()))
			throw new StockAlreadyExistsException("Stock already exists");
		Stock savedStock = stockRepository.save(stock);
		return new ApiResponse(true, "Stock saved successfully", StockResponse.toDto(savedStock));
	}
	
	public ApiResponse getStockById(UUID stockId) {
		Stock stock = stockRepository.findById(stockId).orElseThrow(()-> new StockNotFoundException("Stock not found"));
		return new ApiResponse(true, "Stock retrieved successfully", StockResponse.toDto(stock));
	}

	public ApiResponse getAllStocks() {
		List<Stock> stocks = stockRepository.findAll();
		List<StockResponse> stockResponses = new ArrayList<>();
		for(Stock stock : stocks) {
			stockResponses.add(StockResponse.toDto(stock));
		}
		return new ApiResponse(true, "Stocks retrieved successfully", stockResponses);
	}
	
	public ApiResponse getStockByTicker(String ticker) {
		Stock stock = stockRepository.findByTickerIgnoreCase(ticker.trim()).orElseThrow(()-> new StockNotFoundException("Stock not found"));
		return new ApiResponse(true, "Stock found successfully", StockResponse.toDto(stock));
	}
	
	public ApiResponse updateStockPrice(UUID stockId, BigDecimal newPrice) {
		Stock stock = stockRepository.findById(stockId).orElseThrow(()-> new StockNotFoundException("Stock not found"));
		LocalDateTime now = LocalDateTime.now();
		stock.setLastKnownPrice(newPrice);
		stock.setUpdatedOn(now);
		stock.setLastPriceUpdatedAt(now);
		
		stockRepository.save(stock);
		return new ApiResponse(true, "Stock price updated successfully", StockResponse.toDto(stock));
	}


}
