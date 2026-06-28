package com.bhargav.titantrade.wallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.wallet.service.WalletTransactionService;

@RestController
@RequestMapping("/api/v1/transactions")
public class WalletTransactionController {
	
	private final WalletTransactionService walletTransactionService;
	
	public WalletTransactionController(WalletTransactionService walletTransactionService) {
		this.walletTransactionService = walletTransactionService;
	}
	
	@GetMapping("/")
	public ResponseEntity<ApiResponse> findTransactionsByWallet(){
		return walletTransactionService.findTransactionsByWallet();
	}

}
