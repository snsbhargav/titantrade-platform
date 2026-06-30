package com.bhargav.titantrade.stock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.service.StockService;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {
	
	private final StockService stockService;
	
	public StockController(StockService stockService) {
		this.stockService = stockService;
	}
	
	@PostMapping("/saveStock")
	public ResponseEntity<ApiResponse> saveStock(@RequestBody Stock stock) {
		return new ResponseEntity<ApiResponse>(stockService.saveStock(stock), HttpStatus.OK);
	}

}
