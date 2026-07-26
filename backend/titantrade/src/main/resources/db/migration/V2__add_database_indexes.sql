CREATE INDEX IF NOT EXISTS idx_stock_transactions_user_executed_at
ON stock_transactions (user_id, executed_at DESC);

CREATE INDEX IF NOT EXISTS idx_stock_transactions_user_stock_executed_at
ON stock_transactions (user_id, stock_id, executed_at DESC);

CREATE INDEX IF NOT EXISTS idx_stock_transactions_user_trade_type_executed_at
ON stock_transactions (user_id, trade_type, executed_at DESC);

CREATE INDEX IF NOT EXISTS idx_portfolio_holdings_user_stock
ON portfolio_holdings (user_id, stock_id);

CREATE INDEX IF NOT EXISTS idx_portfolio_holdings_user_quantity
ON portfolio_holdings (user_id, quantity);

CREATE INDEX IF NOT EXISTS idx_wallets_user
ON wallets (user_id);

CREATE INDEX IF NOT EXISTS idx_stocks_ticker
ON stocks (ticker);