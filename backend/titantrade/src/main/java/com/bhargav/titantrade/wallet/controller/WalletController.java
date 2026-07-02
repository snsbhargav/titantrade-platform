package com.bhargav.titantrade.wallet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.wallet.dto.WalletAmountRequest;
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
		return new ResponseEntity<ApiResponse>(walletService.findWalletByUser(), HttpStatus.OK);
	}
	
	@PostMapping("/deposit")
	public ResponseEntity<ApiResponse> depositAmount(@Valid @RequestBody WalletAmountRequest walletAmountRequest){
		return new ResponseEntity<ApiResponse>(walletService.depositAmount(walletAmountRequest), HttpStatus.OK);
	}
	
	@PostMapping("/withdraw")
	public ResponseEntity<ApiResponse> withdrawAmount(@Valid @RequestBody WalletAmountRequest walletAmountRequest){
		return new ResponseEntity<ApiResponse>(walletService.withdrawAmount(walletAmountRequest), HttpStatus.OK);
	}

}
