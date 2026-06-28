package com.bhargav.titantrade.wallet.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.wallet.entity.Wallet;
import com.bhargav.titantrade.wallet.entity.WalletTransaction;


@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID>{
	
	List<WalletTransaction> findByWalletOrderByCreatedOnDesc(Wallet wallet);

}
