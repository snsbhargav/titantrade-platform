package com.bhargav.titantrade.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.portfolio.dto.BuyStockRequest;
import com.bhargav.titantrade.portfolio.service.PortfolioHoldingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {
	
	private final PortfolioHoldingService portfolioHoldingService;
	
	public PortfolioController(PortfolioHoldingService portfolioHoldingService) {
		this.portfolioHoldingService = portfolioHoldingService;
	}
	
	@PostMapping("/buyStock")
	public ResponseEntity<ApiResponse> buyStock(@Valid @RequestBody BuyStockRequest buyStockRequest){
		return portfolioHoldingService.buyStock(buyStockRequest);
		
	}

}
