package com.bhargav.titantrade.stock.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

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

}
