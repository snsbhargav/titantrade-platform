package com.bhargav.titantrade.stock.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.repository.StockRepository;

@Service
public class StockService {

	private final StockRepository stockRepository;

	public StockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	public ApiResponse saveStock(Stock stock) {
		LocalDateTime now = LocalDateTime.now();
		stock.setCreatedOn(now);
		stock.setLastPriceUpdatedAt(now);
		stock.setUpdatedOn(now);

		return new ApiResponse(true, "Stock saved successfully", stockRepository.save(stock));
	}
	
	public ApiResponse getStockById(UUID stockId) {
		Stock stock = stockRepository.findById(stockId).orElseThrow(()-> new StockNotFoundException("Stock not found"));
		return new ApiResponse(true, "Stock retrieved successfully", stock);
	}


}
