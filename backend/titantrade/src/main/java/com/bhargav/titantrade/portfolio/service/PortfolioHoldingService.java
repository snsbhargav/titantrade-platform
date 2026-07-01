package com.bhargav.titantrade.portfolio.service;

import java.math.BigDecimal;
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


	public PortfolioHoldingService(PortfolioHoldingRepository portfolioHoldingRepository, CurrentUserService currentUserService) {
		this.portfolioHoldingRepository = portfolioHoldingRepository;
		this.currentUserService = currentUserService;
	}


	public ApiResponse getMyPortfolio() {
		User user = currentUserService.getCurrentUser();
		List<PortfolioHolding> holdings = portfolioHoldingRepository.findByUserIdAndQuantityGreaterThan(user.getId(),
				BigDecimal.ZERO);
		BigDecimal totalPortfolioValue = BigDecimal.ZERO;
		List<PortfolioHoldingResponse> holdingsDto = new ArrayList<>();
		for (PortfolioHolding holding : holdings) {
			PortfolioHoldingResponse response = PortfolioHoldingResponse.toDto(holding);
			holdingsDto.add(response);
			
			totalPortfolioValue = totalPortfolioValue.add(response.getMarketValue());
		}
		PortfolioSummaryResponse portfolioSummaryResponse = new PortfolioSummaryResponse(holdingsDto, totalPortfolioValue);
		return new ApiResponse(true, "Portfolio retrieved successfully", portfolioSummaryResponse);
	}

}
