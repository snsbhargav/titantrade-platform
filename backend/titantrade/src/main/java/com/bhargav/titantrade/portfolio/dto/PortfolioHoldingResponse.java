package com.bhargav.titantrade.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.portfolio.entity.PortfolioHolding;
import com.bhargav.titantrade.stock.entity.Stock;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioHoldingResponse {
	@NotNull
	private UUID holdingId;
	private UUID stockId;
	private String ticker;
	private String companyName;
	private BigDecimal quantity;
	private BigDecimal averageBuyPrice;
	private BigDecimal currentPrice;
	private BigDecimal marketValue;
	private LocalDateTime createdOn;
	private LocalDateTime updatedOn;
	
	public static PortfolioHoldingResponse toDto(PortfolioHolding portfolioHolding) {
		PortfolioHoldingResponse portfolioHoldingResponse = new PortfolioHoldingResponse();
		Stock stock = portfolioHolding.getStock();
		portfolioHoldingResponse.setHoldingId(portfolioHolding.getId());
		portfolioHoldingResponse.setStockId(stock.getId());
		portfolioHoldingResponse.setTicker(stock.getTicker());
		portfolioHoldingResponse.setCompanyName(stock.getCompanyName());
		portfolioHoldingResponse.setQuantity(portfolioHolding.getQuantity());
		portfolioHoldingResponse.setAverageBuyPrice(portfolioHolding.getAverageBuyPrice());
		portfolioHoldingResponse.setCurrentPrice(stock.getLastKnownPrice());
		portfolioHoldingResponse.setMarketValue(portfolioHolding.getQuantity().multiply(stock.getLastKnownPrice()));
		portfolioHoldingResponse.setCreatedOn(portfolioHolding.getCreatedOn());
		portfolioHoldingResponse.setUpdatedOn(portfolioHolding.getUpdatedOn());
		
		return portfolioHoldingResponse;
	}

}
