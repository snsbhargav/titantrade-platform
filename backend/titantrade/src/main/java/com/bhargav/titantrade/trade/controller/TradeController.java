package com.bhargav.titantrade.trade.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.trade.dto.BuyStockRequest;
import com.bhargav.titantrade.trade.dto.SellStockRequest;
import com.bhargav.titantrade.trade.enums.TradeType;
import com.bhargav.titantrade.trade.service.TradeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {

	private final TradeService tradeService;

	public TradeController(TradeService stockTransactionService) {
		this.tradeService = stockTransactionService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse> getMyTradeHistory(@RequestParam(required = false) UUID stockId,
			@RequestParam(required = false) TradeType tradeType, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return new ResponseEntity<ApiResponse>(tradeService.getMyTradeHistory(stockId, tradeType, page, size), HttpStatus.OK);
	}

	@PostMapping("/buy")
	public ResponseEntity<ApiResponse> buyStock(@Valid @RequestBody BuyStockRequest buyStockRequest) {
		return new ResponseEntity<ApiResponse>(tradeService.buyStock(buyStockRequest), HttpStatus.OK);

	}

	@PostMapping("/sell")
	public ResponseEntity<ApiResponse> sellStock(@Valid @RequestBody SellStockRequest sellStockRequest) {
		return new ResponseEntity<ApiResponse>(tradeService.sellStock(sellStockRequest), HttpStatus.OK);
	}

}
