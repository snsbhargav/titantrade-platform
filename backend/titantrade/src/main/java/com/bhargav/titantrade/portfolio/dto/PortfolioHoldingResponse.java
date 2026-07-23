package com.bhargav.titantrade.portfolio.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.common.constants.DecimalConstants;
import com.bhargav.titantrade.portfolio.entity.PortfolioHolding;
import com.bhargav.titantrade.stock.entity.Stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioHoldingResponse {
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
	private BigDecimal investedValue;
	private BigDecimal unrealizedProfitLoss;
	private BigDecimal unrealizedProfitLossPercentage;

	public static PortfolioHoldingResponse toDto(PortfolioHolding portfolioHolding) {
		PortfolioHoldingResponse portfolioHoldingResponse = new PortfolioHoldingResponse();
		Stock stock = portfolioHolding.getStock();
		BigDecimal stockPrice = stock.getLastKnownPrice().setScale(DecimalConstants.PRICE_SCALE,
				DecimalConstants.ROUNDING_MODE);
		portfolioHoldingResponse.setHoldingId(portfolioHolding.getId());
		portfolioHoldingResponse.setStockId(stock.getId());
		portfolioHoldingResponse.setTicker(stock.getTicker());
		portfolioHoldingResponse.setCompanyName(stock.getCompanyName());
		portfolioHoldingResponse.setQuantity(portfolioHolding.getQuantity().setScale(DecimalConstants.QUANTITY_SCALE,
				DecimalConstants.ROUNDING_MODE));
		portfolioHoldingResponse.setAverageBuyPrice(portfolioHolding.getAverageBuyPrice()
				.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE));
		portfolioHoldingResponse.setCurrentPrice(stockPrice);

		BigDecimal marketValue = portfolioHolding.getQuantity().multiply(stockPrice)
				.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE);
		portfolioHoldingResponse.setMarketValue(marketValue);
		portfolioHoldingResponse.setCreatedOn(portfolioHolding.getCreatedOn());
		portfolioHoldingResponse.setUpdatedOn(portfolioHolding.getUpdatedOn());
		BigDecimal investedValue = portfolioHolding.getAverageBuyPrice().multiply(portfolioHolding.getQuantity())
				.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE);
		portfolioHoldingResponse.setInvestedValue(investedValue);
		BigDecimal unrealizedProfitLoss = portfolioHoldingResponse.getMarketValue()
				.subtract(portfolioHoldingResponse.getInvestedValue())
				.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE);

		portfolioHoldingResponse.setUnrealizedProfitLoss(unrealizedProfitLoss);
		BigDecimal unrealizedProfitLossPercentage = (unrealizedProfitLoss.divide(investedValue,
				DecimalConstants.PERCENTAGE_SCALE, DecimalConstants.ROUNDING_MODE)).multiply(BigDecimal.valueOf(100L));
		portfolioHoldingResponse.setUnrealizedProfitLossPercentage(unrealizedProfitLossPercentage);
		return portfolioHoldingResponse;
	}

}
