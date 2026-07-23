package com.bhargav.titantrade.stock.dto;

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
public class UpdateStockPriceRequest {
	
	@NotNull(message = "Stock price is required")
	@DecimalMin(value = "0.01", message = "Stock price must be greater than zero")
	@Digits(integer = 15, fraction = 4, message = "Stock price can have up to 4 decimal places")
	private BigDecimal price;

}
