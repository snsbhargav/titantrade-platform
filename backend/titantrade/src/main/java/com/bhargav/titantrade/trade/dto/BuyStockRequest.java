package com.bhargav.titantrade.trade.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyStockRequest {
	
	@NotNull(message = "Stock id is required")
	private UUID stockId;
	@NotNull(message = "Quantity is required")
	@DecimalMin(value = "0.000001", message = "Quantity must be greater than zero.")
	@Digits(integer = 13, fraction = 6, message = "Quantity can have up to 6 decimal places")
	private BigDecimal quantity;

}
