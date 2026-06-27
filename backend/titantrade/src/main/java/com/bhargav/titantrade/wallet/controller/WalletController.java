package com.bhargav.titantrade.wallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.wallet.service.WalletService;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {
	

	private final WalletService walletService;
	
	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}
	
	@GetMapping("/walletBalance")
	public ResponseEntity<ApiResponse> getWallet() { 
		return walletService.findWalletByUser();
	}

}
