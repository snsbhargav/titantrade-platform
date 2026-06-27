package com.bhargav.titantrade.wallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.wallet.dto.DepositRequest;
import com.bhargav.titantrade.wallet.service.WalletService;

import jakarta.validation.Valid;

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
	
	@PostMapping("/deposit")
	public ResponseEntity<ApiResponse> depositAmount(@Valid @RequestBody DepositRequest depositRequest){
		return walletService.depositAmount(depositRequest);
	}

}
