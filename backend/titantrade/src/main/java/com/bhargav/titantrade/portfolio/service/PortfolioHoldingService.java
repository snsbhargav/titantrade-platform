package com.bhargav.titantrade.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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
		BigDecimal totalPortfolioValue = BigDecimal.ZERO;
		BigDecimal totalInvestedValue = BigDecimal.ZERO;
		BigDecimal totalUnrealizedProfitLoss = BigDecimal.ZERO;
		BigDecimal totalUnrealizedProfitLossPercentage = BigDecimal.ZERO;
		List<PortfolioHoldingResponse> holdingsDto = new ArrayList<>();
		for (PortfolioHolding holding : holdings) {
			PortfolioHoldingResponse response = PortfolioHoldingResponse.toDto(holding);
			holdingsDto.add(response);

			totalPortfolioValue = totalPortfolioValue.add(response.getMarketValue());
			totalInvestedValue = totalInvestedValue.add(response.getInvestedValue());
			totalUnrealizedProfitLoss = totalUnrealizedProfitLoss.add(response.getUnrealizedProfitLoss());
			totalUnrealizedProfitLossPercentage = (totalUnrealizedProfitLoss.divide(totalInvestedValue, 4, RoundingMode.HALF_UP))
					.multiply(BigDecimal.valueOf(100));
		}
		PortfolioSummaryResponse portfolioSummaryResponse = new PortfolioSummaryResponse(holdingsDto,
				totalPortfolioValue, totalInvestedValue, totalUnrealizedProfitLoss,
				totalUnrealizedProfitLossPercentage);
		return new ApiResponse(true, "Portfolio retrieved successfully", portfolioSummaryResponse);
	}

}
