package com.bhargav.titantrade.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.constants.DecimalConstants;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.portfolio.dto.PortfolioHoldingResponse;
import com.bhargav.titantrade.portfolio.dto.PortfolioSummaryResponse;
import com.bhargav.titantrade.portfolio.entity.PortfolioHolding;
import com.bhargav.titantrade.portfolio.repository.PortfolioHoldingRepository;
import com.bhargav.titantrade.user.entity.User;

@Service
public class PortfolioHoldingService {

	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final CurrentUserService currentUserService;

	public PortfolioHoldingService(PortfolioHoldingRepository portfolioHoldingRepository,
			CurrentUserService currentUserService) {
		this.portfolioHoldingRepository = portfolioHoldingRepository;
		this.currentUserService = currentUserService;
	}

	public ApiResponse getMyPortfolio() {
		User user = currentUserService.getCurrentUser();
		List<PortfolioHolding> holdings = portfolioHoldingRepository.findByUserIdAndQuantityGreaterThan(user.getId(),
				BigDecimal.ZERO);
		BigDecimal totalPortfolioValue = BigDecimal.ZERO.setScale(DecimalConstants.PRICE_SCALE,
				DecimalConstants.ROUNDING_MODE);
		BigDecimal totalInvestedValue = BigDecimal.ZERO.setScale(DecimalConstants.PRICE_SCALE,
				DecimalConstants.ROUNDING_MODE);
		BigDecimal totalUnrealizedProfitLoss = BigDecimal.ZERO.setScale(DecimalConstants.PRICE_SCALE,
				DecimalConstants.ROUNDING_MODE);
		BigDecimal totalUnrealizedProfitLossPercentage = BigDecimal.ZERO.setScale(DecimalConstants.PERCENTAGE_SCALE,
				DecimalConstants.ROUNDING_MODE);
		List<PortfolioHoldingResponse> holdingsDto = new ArrayList<>();
		for (PortfolioHolding holding : holdings) {
			PortfolioHoldingResponse response = PortfolioHoldingResponse.toDto(holding);
			holdingsDto.add(response);

			totalPortfolioValue = totalPortfolioValue.add(response.getMarketValue())
					.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE);
			totalInvestedValue = totalInvestedValue.add(response.getInvestedValue())
					.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE);
			totalUnrealizedProfitLoss = totalUnrealizedProfitLoss.add(response.getUnrealizedProfitLoss())
					.setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE);

		}
		if (totalInvestedValue.compareTo(BigDecimal.ZERO) > 0) {
			totalUnrealizedProfitLossPercentage = (totalUnrealizedProfitLoss.multiply(BigDecimal.valueOf(100))
					.divide(totalInvestedValue, DecimalConstants.PERCENTAGE_SCALE, DecimalConstants.ROUNDING_MODE));
		}
		PortfolioSummaryResponse portfolioSummaryResponse = new PortfolioSummaryResponse(holdingsDto,
				totalPortfolioValue, totalInvestedValue, totalUnrealizedProfitLoss,
				totalUnrealizedProfitLossPercentage);
		return new ApiResponse(true, "Portfolio retrieved successfully", portfolioSummaryResponse);
	}

}
