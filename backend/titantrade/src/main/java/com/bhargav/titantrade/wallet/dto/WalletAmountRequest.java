package com.bhargav.titantrade.wallet.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletAmountRequest {
	
	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be atleast 0.01.")
	@Digits(integer = 17, fraction = 2, message = "Amount can have up to 2 decimal places")
	private BigDecimal amount;

}
