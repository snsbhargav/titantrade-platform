package com.bhargav.titantrade.stock.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.stock.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID>{

}
