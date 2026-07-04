package com.bhargav.titantrade.stock.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.stock.dto.CreateStockRequest;
import com.bhargav.titantrade.stock.dto.UpdateStockPriceRequest;
import com.bhargav.titantrade.stock.service.StockService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

	private final StockService stockService;

	public StockController(StockService stockService) {
		this.stockService = stockService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse> saveStock(@Valid @RequestBody CreateStockRequest stockDto) {
		return new ResponseEntity<ApiResponse>(stockService.saveStock(stockDto), HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<ApiResponse> getStocks(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(required = false, defaultValue = "10") int size,
			@RequestParam(required = false) String search) {
		return new ResponseEntity<ApiResponse>(stockService.getAllStocks(page, size, search), HttpStatus.OK);
	}

	@GetMapping("/{stockId}")
	public ResponseEntity<ApiResponse> getStockById(@PathVariable UUID stockId) {
		return new ResponseEntity<ApiResponse>(stockService.getStockById(stockId), HttpStatus.OK);
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse> getStockByTicker(@RequestParam String ticker) {
		return new ResponseEntity<ApiResponse>(stockService.getStockByTicker(ticker), HttpStatus.OK);
	}

	@PutMapping("/{stockId}/price")
	public ResponseEntity<ApiResponse> updateStockPrice(@PathVariable(name = "stockId") UUID stockId,
			@Valid @RequestBody UpdateStockPriceRequest priceRequest) {
		return new ResponseEntity<ApiResponse>(stockService.updateStockPrice(stockId, priceRequest.getPrice()),
				HttpStatus.OK);
	}

}
