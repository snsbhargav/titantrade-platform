package com.bhargav.titantrade.wallet.dto;

import java.math.BigDecimal;

import com.bhargav.titantrade.wallet.enums.CurrencyType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletBalanceResponse {
	
	private BigDecimal balance;
	
	private CurrencyType currency;

}
