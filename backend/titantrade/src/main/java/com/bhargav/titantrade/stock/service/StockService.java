package com.bhargav.titantrade.stock.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.exception.StockAlreadyExistsException;
import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.stock.dto.CreateStockRequest;
import com.bhargav.titantrade.stock.dto.StockListResponse;
import com.bhargav.titantrade.stock.dto.StockResponse;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.repository.StockRepository;
import com.bhargav.titantrade.stock.specification.StockSpecification;

@Service
public class StockService {

	private final StockRepository stockRepository;

	public StockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	public ApiResponse saveStock(CreateStockRequest stockDto) {
		Stock stock = CreateStockRequest.toEntity(stockDto);
		if (stockRepository.existsByTickerIgnoreCase(stock.getTicker().trim()))
			throw new StockAlreadyExistsException("Stock already exists");
		Stock savedStock = stockRepository.save(stock);
		return new ApiResponse(true, "Stock saved successfully", StockResponse.toDto(savedStock));
	}

	public ApiResponse getStockById(UUID stockId) {
		Stock stock = stockRepository.findById(stockId)
				.orElseThrow(() -> new StockNotFoundException("Stock not found"));
		return new ApiResponse(true, "Stock retrieved successfully", StockResponse.toDto(stock));
	}

	public ApiResponse getAllStocks(int page, int size, String search) {
		if(page<0) page=0;
		if(size<=0) size=10;
		if(size>100) size=100;
		Specification<Stock> specification = Specification.allOf(StockSpecification.searchByTickerOrCompanyName(search),
				StockSpecification.isActive());
		Pageable pageable = PageRequest.of(page, size, Sort.by("ticker").ascending());
		Page<Stock> stocks = stockRepository.findAll(specification, pageable);
		
		//Change entity into DTOs
		List<StockResponse> stockResponses = new ArrayList<>();
		for (Stock stock : stocks.getContent()) {
			stockResponses.add(StockResponse.toDto(stock));
		}
		
		//Create Response Object
		StockListResponse response = new StockListResponse();
		response.setPage(page);
		response.setSize(size);
		response.setStocks(stockResponses);
		response.setTotalElements(stocks.getTotalElements());
		response.setLast(stocks.isLast());
		response.setTotalPages(stocks.getTotalPages());
		return new ApiResponse(true, "Stocks retrieved successfully", response);
	}

	public ApiResponse getStockByTicker(String ticker) {
		Stock stock = stockRepository.findByTickerIgnoreCase(ticker.trim())
				.orElseThrow(() -> new StockNotFoundException("Stock not found"));
		return new ApiResponse(true, "Stock found successfully", StockResponse.toDto(stock));
	}

	public ApiResponse updateStockPrice(UUID stockId, BigDecimal newPrice) {
		Stock stock = stockRepository.findById(stockId)
				.orElseThrow(() -> new StockNotFoundException("Stock not found"));
		LocalDateTime now = LocalDateTime.now();
		stock.setLastKnownPrice(newPrice);
		stock.setLastPriceUpdatedAt(now);

		stockRepository.save(stock);
		return new ApiResponse(true, "Stock price updated successfully", StockResponse.toDto(stock));
	}

}
