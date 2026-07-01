package com.bhargav.titantrade.trade.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.trade.service.StockTransactionService;

@RestController
@RequestMapping("/api/v1/trades")
public class StockTransactionController {
	
	private final StockTransactionService stockTransactionService;
	public StockTransactionController(StockTransactionService stockTransactionService) {
		this.stockTransactionService = stockTransactionService;
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse> getMyTradeHistory(@RequestParam(required = false) UUID stockId){
		return new ResponseEntity<ApiResponse>(stockTransactionService.getMyTradeHistory(stockId), HttpStatus.OK);
	}

}
