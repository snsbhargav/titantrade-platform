package com.bhargav.titantrade.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.enums.AssetType;
import com.bhargav.titantrade.wallet.enums.CurrencyType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockResponse {

	private UUID id;
	private String ticker;
	private String companyName;
	private BigDecimal lastKnownPrice;
	private LocalDateTime lastPriceUpdatedAt;
	private String exchange;
	private CurrencyType currency;
	private String sector;
	private AssetType assetType;
	private boolean isActive;
	private LocalDateTime createdOn;
	private LocalDateTime updatedOn;
	
	public static StockResponse toDto(Stock stock) {
		StockResponse response = new StockResponse();
		response.setId(stock.getId());
		response.setTicker(stock.getTicker());
		response.setCompanyName(stock.getCompanyName());
		response.setLastKnownPrice(stock.getLastKnownPrice());
		response.setLastPriceUpdatedAt(stock.getLastPriceUpdatedAt());
		response.setExchange(stock.getExchange());
		response.setCurrency(stock.getCurrency());
		response.setSector(stock.getSector());
		response.setAssetType(stock.getAssetType());
		response.setActive(stock.isActive());
		response.setCreatedOn(stock.getCreatedOn());
		response.setUpdatedOn(stock.getUpdatedOn());
		
		return response;
	}
}
