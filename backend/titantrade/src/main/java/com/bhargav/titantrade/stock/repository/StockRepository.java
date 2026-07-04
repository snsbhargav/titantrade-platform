package com.bhargav.titantrade.stock.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.stock.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID>, JpaSpecificationExecutor<Stock>{

	Optional<Stock> findByTicker(String ticker);
	
	Optional<Stock> findByTickerIgnoreCase(String ticker);

	boolean existsByTickerIgnoreCase(String ticker);
	
	Page<Stock> findAll(Pageable pageable);
	
	

}
