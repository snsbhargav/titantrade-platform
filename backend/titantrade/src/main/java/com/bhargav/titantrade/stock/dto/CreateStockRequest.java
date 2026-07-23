package com.bhargav.titantrade.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bhargav.titantrade.common.constants.DecimalConstants;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.enums.AssetType;
import com.bhargav.titantrade.wallet.enums.CurrencyType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStockRequest {
	@NotBlank(message = "Ticker is required")
	private String ticker;
	@NotBlank(message = "Company name is required")
	private String companyName;
	@NotNull(message = "Last known price is required")
	@DecimalMin(value = "0.01", message = "Last known price must be greater than zero")
	@Digits(integer = 15, fraction = 4, message = "Last known price can have upto 4 decimal places")
	private BigDecimal lastKnownPrice;
	@NotBlank(message = "Exchange is required")
	private String exchange;
	@NotNull(message = "Currency is required")
	private CurrencyType currency;
	@NotBlank(message = "Sector is required")
	private String sector;
	@NotNull(message = "Asset type is required")
	private AssetType assetType;

	public static Stock toEntity(CreateStockRequest request) {
		Stock stock = new Stock();
		stock.setTicker(request.getTicker().trim().toUpperCase());
		stock.setCompanyName(request.getCompanyName().trim());
		stock.setLastKnownPrice(
				request.getLastKnownPrice().setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE));
		stock.setExchange(request.getExchange().trim().toUpperCase());
		stock.setCurrency(request.getCurrency());
		stock.setSector(request.getSector().trim());
		stock.setAssetType(request.getAssetType());
		stock.setActive(true);

		return stock;
	}

}
