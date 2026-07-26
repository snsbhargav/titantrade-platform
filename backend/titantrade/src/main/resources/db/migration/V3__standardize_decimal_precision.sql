ALTER TABLE wallets
ALTER COLUMN balance TYPE numeric(19,2);

ALTER TABLE wallet_transactions
ALTER COLUMN amount TYPE numeric(19,2);

ALTER TABLE wallet_transactions
ALTER COLUMN balance_after_transaction TYPE numeric(19,2);

ALTER TABLE stocks
ALTER COLUMN last_known_price TYPE numeric(19,4);

ALTER TABLE stock_transactions
ALTER COLUMN quantity TYPE numeric(19,6);

ALTER TABLE stock_transactions
ALTER COLUMN price_per_share TYPE numeric(19,4);

ALTER TABLE stock_transactions
ALTER COLUMN total_amount TYPE numeric(19,4);

ALTER TABLE portfolio_holdings
ALTER COLUMN quantity TYPE numeric(19,6);

ALTER TABLE portfolio_holdings
ALTER COLUMN average_buy_price TYPE numeric(19,4);