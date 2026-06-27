package com.bhargav.titantrade.wallet.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.wallet.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID>{
	Optional<Wallet> findByUser(User user);
}
