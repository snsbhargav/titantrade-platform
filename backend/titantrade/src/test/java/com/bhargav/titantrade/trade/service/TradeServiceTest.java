package com.bhargav.titantrade.trade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.bhargav.titantrade.common.exception.InsufficientFundsException;
import com.bhargav.titantrade.common.exception.StockNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.portfolio.entity.PortfolioHolding;
import com.bhargav.titantrade.portfolio.repository.PortfolioHoldingRepository;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.stock.enums.AssetType;
import com.bhargav.titantrade.stock.repository.StockRepository;
import com.bhargav.titantrade.stock.service.StockService;
import com.bhargav.titantrade.trade.dto.BuyStockRequest;
import com.bhargav.titantrade.trade.dto.SellStockRequest;
import com.bhargav.titantrade.trade.dto.TradeHistoryResponse;
import com.bhargav.titantrade.trade.entity.Order;
import com.bhargav.titantrade.trade.entity.StockTransaction;
import com.bhargav.titantrade.trade.enums.OrderStatus;
import com.bhargav.titantrade.trade.enums.TradeStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import com.bhargav.titantrade.trade.repository.OrderRepository;
import com.bhargav.titantrade.trade.repository.StockTransactionRepository;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.wallet.enums.CurrencyType;
import com.bhargav.titantrade.wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private StockService stockService;

    @Mock
    private WalletService walletService;

    @Mock
    private PortfolioHoldingRepository portfolioHoldingRepository;

    @Mock
    private StockTransactionRepository stockTransactionRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private TradeService tradeService;

    private UUID userId;
    private UUID stockId;

    @BeforeEach
    void setUp() {
        stockId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    private User createUser() {
        User user = new User();
        user.setId(userId);
        return user;
    }

    private Stock createdTempStock(BigDecimal price) {
        Stock stock = new Stock();
        stock.setId(stockId);
        stock.setCompanyName("Apple");
        stock.setTicker("AAPL");
        stock.setAssetType(AssetType.STOCK);
        stock.setCurrency(CurrencyType.USD);
        stock.setLastKnownPrice(price.setScale(4));
        stock.setActive(true);
        return stock;
    }

    private PortfolioHolding createPortfolioHolding(Stock stock, User user) {
        PortfolioHolding holding = new PortfolioHolding();
        holding.setId(UUID.randomUUID());
        holding.setQuantity(BigDecimal.valueOf(2));
        holding.setStock(stock);
        holding.setUser(user);
        holding.setAverageBuyPrice(BigDecimal.valueOf(100).setScale(4));
        return holding;
    }

    @Test
    void buyStock_shouldCreateNewHolding_whenUserDoesNotOwnStockYet() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(190));
        UUID idempotencyKey = UUID.randomUUID();

        BuyStockRequest buyStockRequest =
                new BuyStockRequest(stockId, BigDecimal.ONE, idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(portfolioHoldingRepository.findByUserIdAndStockId(userId, stockId))
                .thenReturn(Optional.empty());

        List<OrderStatus> savedStatuses = new ArrayList<>();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedStatuses.add(order.getOrderStatus());
            return order;
        });

        ApiResponse response = tradeService.buyStock(buyStockRequest);

        assertTrue(response.isSuccess());
        assertEquals("Buy order executed successfully", response.getMessage());

        verify(orderRepository, times(2)).save(any(Order.class));

        assertEquals(2, savedStatuses.size());
        assertEquals(OrderStatus.PENDING, savedStatuses.get(0));
        assertEquals(OrderStatus.EXECUTED, savedStatuses.get(1));

        verify(walletService).debitCurrentUserWallet(
                argThat(amount -> amount.compareTo(BigDecimal.valueOf(190).setScale(2)) == 0)
        );

        verify(portfolioHoldingRepository).save(argThat(holding ->
                holding.getUser().equals(user)
                        && holding.getStock().equals(stock)
                        && holding.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && holding.getAverageBuyPrice().compareTo(stock.getLastKnownPrice()) == 0
        ));

        verify(stockTransactionRepository).save(argThat(transaction ->
                transaction.getStock().equals(stock)
                        && transaction.getUser().equals(user)
                        && transaction.getPricePerShare().compareTo(stock.getLastKnownPrice()) == 0
                        && transaction.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && transaction.getTradeStatus().equals(TradeStatus.SUCCESS)
                        && transaction.getTradeType().equals(TradeType.BUY)
                        && transaction.getTotalAmount().compareTo(BigDecimal.valueOf(190).setScale(4)) == 0
        ));
    }

    @Test
    void buyStock_shouldUpdateExistingHoldingAndRecalculateAveragePrice_whenUserAlreadyOwnsStock() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(200));
        PortfolioHolding holding = createPortfolioHolding(stock, user);
        UUID idempotencyKey = UUID.randomUUID();

        BuyStockRequest buyStockRequest =
                new BuyStockRequest(stockId, BigDecimal.valueOf(2), idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(portfolioHoldingRepository.findByUserIdAndStockId(userId, stockId))
                .thenReturn(Optional.of(holding));

        List<OrderStatus> savedStatuses = new ArrayList<>();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedStatuses.add(order.getOrderStatus());
            return order;
        });

        ApiResponse response = tradeService.buyStock(buyStockRequest);

        assertTrue(response.isSuccess());
        assertEquals("Buy order executed successfully", response.getMessage());

        verify(orderRepository, times(2)).save(any(Order.class));

        assertEquals(2, savedStatuses.size());
        assertEquals(OrderStatus.PENDING, savedStatuses.get(0));
        assertEquals(OrderStatus.EXECUTED, savedStatuses.get(1));

        verify(walletService).debitCurrentUserWallet(
                argThat(amount -> amount.compareTo(BigDecimal.valueOf(400).setScale(2)) == 0)
        );

        verify(portfolioHoldingRepository).save(argThat(savedHolding ->
                savedHolding.getId().equals(holding.getId())
                        && savedHolding.getStock().equals(stock)
                        && savedHolding.getUser().equals(user)
                        && savedHolding.getQuantity().compareTo(BigDecimal.valueOf(4).setScale(6)) == 0
                        && savedHolding.getAverageBuyPrice().compareTo(BigDecimal.valueOf(150).setScale(4)) == 0
        ));

        verify(stockTransactionRepository).save(argThat(transaction ->
                transaction.getStock().equals(stock)
                        && transaction.getUser().equals(user)
                        && transaction.getPricePerShare().compareTo(stock.getLastKnownPrice()) == 0
                        && transaction.getQuantity().compareTo(buyStockRequest.getQuantity()) == 0
                        && transaction.getTradeStatus().equals(TradeStatus.SUCCESS)
                        && transaction.getTradeType().equals(TradeType.BUY)
                        && transaction.getTotalAmount().compareTo(BigDecimal.valueOf(400).setScale(4)) == 0
        ));
    }

    @Test
    void buyStock_shouldRejectOrder_whenStockIsInactive() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(200));
        stock.setActive(false);

        UUID idempotencyKey = UUID.randomUUID();

        BuyStockRequest request =
                new BuyStockRequest(stockId, BigDecimal.valueOf(2), idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));

        List<OrderStatus> savedStatuses = new ArrayList<>();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedStatuses.add(order.getOrderStatus());
            return order;
        });

        ApiResponse response = tradeService.buyStock(request);

        assertFalse(response.isSuccess());
        assertEquals("Stock is inactive and cannot be traded", response.getMessage());

        verify(orderRepository, times(2)).save(any(Order.class));

        assertEquals(2, savedStatuses.size());
        assertEquals(OrderStatus.PENDING, savedStatuses.get(0));
        assertEquals(OrderStatus.REJECTED, savedStatuses.get(1));

        verify(walletService, never()).debitCurrentUserWallet(any());
        verify(portfolioHoldingRepository, never()).save(any());
        verify(stockTransactionRepository, never()).save(any());
    }

    @Test
    void buyStock_shouldThrowStockNotFoundException_whenStockDoesNotExist() {
        User user = createUser();
        UUID idempotencyKey = UUID.randomUUID();

        BuyStockRequest request =
                new BuyStockRequest(stockId, BigDecimal.ONE, idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> tradeService.buyStock(request));

        verify(orderRepository, never()).save(any(Order.class));
        verify(walletService, never()).debitCurrentUserWallet(any());
        verify(portfolioHoldingRepository, never()).save(any());
        verify(stockTransactionRepository, never()).save(any());
    }

    @Test
    void buyStock_shouldRejectOrder_whenWalletWithdrawFails() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(200));
        UUID idempotencyKey = UUID.randomUUID();

        BuyStockRequest request =
                new BuyStockRequest(stockId, BigDecimal.valueOf(2), idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));

        List<OrderStatus> savedStatuses = new ArrayList<>();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedStatuses.add(order.getOrderStatus());
            return order;
        });

        doThrow(new InsufficientFundsException("Insufficient funds"))
                .when(walletService)
                .debitCurrentUserWallet(any(BigDecimal.class));

        ApiResponse response = tradeService.buyStock(request);

        assertFalse(response.isSuccess());
        assertEquals("Insufficient funds", response.getMessage());

        verify(walletService).debitCurrentUserWallet(
                argThat(amount -> amount.compareTo(BigDecimal.valueOf(400).setScale(2)) == 0)
        );

        verify(portfolioHoldingRepository, never()).save(any());
        verify(portfolioHoldingRepository, never()).findByUserIdAndStockId(any(), any());
        verify(stockTransactionRepository, never()).save(any());

        verify(orderRepository, times(2)).save(any(Order.class));

        assertEquals(2, savedStatuses.size());
        assertEquals(OrderStatus.PENDING, savedStatuses.get(0));
        assertEquals(OrderStatus.REJECTED, savedStatuses.get(1));
    }

    @Test
    void sellStock_shouldUpdatePortfolioHoldings_WhenUserHasSufficientBalance() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(200));
        PortfolioHolding holding = createPortfolioHolding(stock, user);
        UUID idempotencyKey = UUID.randomUUID();

        SellStockRequest request =
                new SellStockRequest(stockId, BigDecimal.ONE, idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(portfolioHoldingRepository.findByUserIdAndStockId(userId, stockId))
                .thenReturn(Optional.of(holding));

        List<OrderStatus> savedStatuses = new ArrayList<>();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedStatuses.add(order.getOrderStatus());
            return order;
        });

        ApiResponse response = tradeService.sellStock(request);

        assertTrue(response.isSuccess());
        assertEquals("Sell order executed successfully", response.getMessage());

        verify(orderRepository, times(2)).save(any(Order.class));

        assertEquals(2, savedStatuses.size());
        assertEquals(OrderStatus.PENDING, savedStatuses.get(0));
        assertEquals(OrderStatus.EXECUTED, savedStatuses.get(1));

        verify(portfolioHoldingRepository).save(argThat(savedHolding ->
                savedHolding.getId().equals(holding.getId())
                        && savedHolding.getQuantity().compareTo(BigDecimal.ONE.setScale(6)) == 0
                        && savedHolding.getUser().equals(user)
                        && savedHolding.getStock().equals(stock)
                        && savedHolding.getAverageBuyPrice().compareTo(holding.getAverageBuyPrice()) == 0
        ));

        verify(walletService).creditCurrentUserWallet(
                argThat(amount -> amount.compareTo(BigDecimal.valueOf(200).setScale(2)) == 0)
        );

        verify(stockTransactionRepository).save(argThat(transaction ->
                transaction.getQuantity().compareTo(request.getQuantity()) == 0
                        && transaction.getPricePerShare().compareTo(stock.getLastKnownPrice()) == 0
                        && transaction.getTradeStatus().equals(TradeStatus.SUCCESS)
                        && transaction.getTradeType().equals(TradeType.SELL)
                        && transaction.getStock().equals(stock)
                        && transaction.getUser().equals(user)
                        && transaction.getTotalAmount().compareTo(BigDecimal.valueOf(200).setScale(4)) == 0
        ));
    }

    @Test
    void sellStock_shouldThrowStockNotFoundException_whenStockDoesNotExist() {
        User user = createUser();
        UUID idempotencyKey = UUID.randomUUID();

        SellStockRequest request =
                new SellStockRequest(stockId, BigDecimal.valueOf(200), idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> tradeService.sellStock(request));

        verify(orderRepository, never()).save(any(Order.class));
        verify(portfolioHoldingRepository, never()).save(any());
        verify(portfolioHoldingRepository, never()).findByUserIdAndStockId(any(), any());
        verify(walletService, never()).creditCurrentUserWallet(any());
        verify(stockTransactionRepository, never()).save(any());
    }

    @Test
    void sellStock_shouldRejectOrder_whenUserDoesNotOwnStock() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(200));
        UUID idempotencyKey = UUID.randomUUID();

        SellStockRequest request =
                new SellStockRequest(stockId, BigDecimal.valueOf(2), idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(portfolioHoldingRepository.findByUserIdAndStockId(userId, stockId))
                .thenReturn(Optional.empty());

        List<OrderStatus> savedStatuses = new ArrayList<>();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedStatuses.add(order.getOrderStatus());
            return order;
        });

        ApiResponse response = tradeService.sellStock(request);

        assertFalse(response.isSuccess());
        assertEquals("Portfolio not found", response.getMessage());

        verify(orderRepository, times(2)).save(any(Order.class));

        assertEquals(2, savedStatuses.size());
        assertEquals(OrderStatus.PENDING, savedStatuses.get(0));
        assertEquals(OrderStatus.REJECTED, savedStatuses.get(1));

        verify(portfolioHoldingRepository, never()).save(any());
        verify(walletService, never()).creditCurrentUserWallet(any());
        verify(stockTransactionRepository, never()).save(any());
    }

    @Test
    void sellStock_shouldRejectOrder_whenSellQuantityExceedsHoldingQuantity() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(200));
        PortfolioHolding holding = createPortfolioHolding(stock, user);
        UUID idempotencyKey = UUID.randomUUID();

        SellStockRequest request =
                new SellStockRequest(stockId, BigDecimal.valueOf(20), idempotencyKey);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(portfolioHoldingRepository.findByUserIdAndStockId(userId, stockId))
                .thenReturn(Optional.of(holding));

        List<OrderStatus> savedStatuses = new ArrayList<>();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedStatuses.add(order.getOrderStatus());
            return order;
        });

        ApiResponse response = tradeService.sellStock(request);

        assertFalse(response.isSuccess());
        assertEquals("Insufficient holdings", response.getMessage());

        verify(orderRepository, times(2)).save(any(Order.class));

        assertEquals(2, savedStatuses.size());
        assertEquals(OrderStatus.PENDING, savedStatuses.get(0));
        assertEquals(OrderStatus.REJECTED, savedStatuses.get(1));

        verify(portfolioHoldingRepository, never()).save(any());
        verify(walletService, never()).creditCurrentUserWallet(any());
        verify(stockTransactionRepository, never()).save(any());
    }

    @Test
    void getMyTradeHistory_shouldReturnPaginatedTradeHistory_whenNoFiltersProvided() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(100));

        StockTransaction transaction1 = new StockTransaction(
                UUID.randomUUID(),
                user,
                stock,
                TradeType.BUY,
                BigDecimal.ONE,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                TradeStatus.SUCCESS,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        StockTransaction transaction2 = new StockTransaction(
                UUID.randomUUID(),
                user,
                stock,
                TradeType.SELL,
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                TradeStatus.FAILED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("executedAt").descending());
        Page<StockTransaction> transactions = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(stockTransactionRepository.findByUserId(userId, pageable)).thenReturn(transactions);

        ApiResponse response = tradeService.getMyTradeHistory(null, null, 0, 10);

        assertTrue(response.isSuccess());
        assertEquals("Stock transactions retrieved successfully", response.getMessage());

        TradeHistoryResponse history = (TradeHistoryResponse) response.getData();

        assertEquals(0, history.getPage());
        assertEquals(10, history.getSize());
        assertEquals(2, history.getTotalElements());
        assertEquals(2, history.getTrades().size());
    }

    @Test
    void getMyTradeHistory_shouldReturnTradesFilteredByTradeType_whenTradeTypeProvided() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(100));

        StockTransaction transaction1 = new StockTransaction(
                UUID.randomUUID(),
                user,
                stock,
                TradeType.BUY,
                BigDecimal.ONE,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                TradeStatus.SUCCESS,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        StockTransaction transaction2 = new StockTransaction(
                UUID.randomUUID(),
                user,
                stock,
                TradeType.BUY,
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(1000),
                TradeStatus.FAILED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("executedAt").descending());
        Page<StockTransaction> transactions = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(stockTransactionRepository.findByUserIdAndTradeType(userId, TradeType.BUY, pageable))
                .thenReturn(transactions);

        ApiResponse response = tradeService.getMyTradeHistory(null, TradeType.BUY, 0, 10);

        assertTrue(response.isSuccess());
        assertEquals("Stock transactions retrieved successfully", response.getMessage());

        TradeHistoryResponse history = (TradeHistoryResponse) response.getData();

        assertEquals(0, history.getPage());
        assertEquals(10, history.getSize());
        assertEquals(2, history.getTotalElements());
        assertEquals(2, history.getTrades().size());
    }

    @Test
    void getMyTradeHistory_shouldReturnTradesFilteredByStock_whenStockIdProvided() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(100));

        StockTransaction transaction1 = new StockTransaction(
                UUID.randomUUID(),
                user,
                stock,
                TradeType.BUY,
                BigDecimal.ONE,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                TradeStatus.SUCCESS,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        StockTransaction transaction2 = new StockTransaction(
                UUID.randomUUID(),
                user,
                stock,
                TradeType.SELL,
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(1000),
                TradeStatus.FAILED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("executedAt").descending());
        Page<StockTransaction> transactions = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(stockService.getStockById(stockId))
                .thenReturn(new ApiResponse(true, "stock retrieved successfully", stock));
        when(stockTransactionRepository.findByUserIdAndStockId(userId, stockId, pageable))
                .thenReturn(transactions);

        ApiResponse response = tradeService.getMyTradeHistory(stockId, null, 0, 10);

        assertTrue(response.isSuccess());
        assertEquals("Stock transactions retrieved successfully", response.getMessage());

        TradeHistoryResponse history = (TradeHistoryResponse) response.getData();

        assertEquals(0, history.getPage());
        assertEquals(10, history.getSize());
        assertEquals(2, history.getTotalElements());
        assertEquals(2, history.getTrades().size());

        verify(stockTransactionRepository, never()).findByUserId(any(), any());
        verify(stockTransactionRepository, never()).findByUserIdAndTradeType(any(), any(), any());
        verify(stockTransactionRepository, never()).findByUserIdAndStockIdAndTradeType(any(), any(), any(), any());
    }

    @Test
    void getMyTradeHistory_shouldReturnTradesFilteredByStockAndTradeType_whenBothFiltersProvided() {
        User user = createUser();
        Stock stock = createdTempStock(BigDecimal.valueOf(100));

        StockTransaction transaction1 = new StockTransaction(
                UUID.randomUUID(),
                user,
                stock,
                TradeType.SELL,
                BigDecimal.ONE,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                TradeStatus.SUCCESS,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        StockTransaction transaction2 = new StockTransaction(
                UUID.randomUUID(),
                user,
                stock,
                TradeType.SELL,
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(1000),
                TradeStatus.FAILED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("executedAt").descending());
        Page<StockTransaction> transactions = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(stockService.getStockById(stockId))
                .thenReturn(new ApiResponse(true, "stock retrieved successfully", stock));
        when(stockTransactionRepository.findByUserIdAndStockIdAndTradeType(userId, stockId, TradeType.SELL, pageable))
                .thenReturn(transactions);

        ApiResponse response = tradeService.getMyTradeHistory(stockId, TradeType.SELL, 0, 10);

        assertTrue(response.isSuccess());
        assertEquals("Stock transactions retrieved successfully", response.getMessage());

        TradeHistoryResponse history = (TradeHistoryResponse) response.getData();

        assertEquals(0, history.getPage());
        assertEquals(10, history.getSize());
        assertEquals(2, history.getTotalElements());
        assertEquals(2, history.getTrades().size());

        verify(stockTransactionRepository, never()).findByUserId(any(), any());
        verify(stockTransactionRepository, never()).findByUserIdAndTradeType(any(), any(), any());
        verify(stockTransactionRepository, never()).findByUserIdAndStockId(any(), any(), any());
    }
}