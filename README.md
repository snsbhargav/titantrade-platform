# TitanTrade

TitanTrade is a full-stack paper trading and portfolio management platform built with Java, Spring Boot, PostgreSQL, Spring Security JWT, and React.

The goal of TitanTrade is to simulate a real trading platform where users can register, manage wallet balances, browse stocks, execute buy/sell trades, track portfolio holdings, and view trade history with pagination and filters.

---

## Features

- User registration and login with JWT authentication
- Role-based access control for `CUSTOMER` and `ADMIN`
- Admin-only stock creation and stock price update APIs
- Wallet deposit and withdrawal operations
- Stock browsing with pagination and search
- Active stock listing for tradable stocks
- Buy and sell stock workflows
- Weighted average buy price calculation for existing holdings
- Portfolio holdings and unrealized profit/loss calculations
- Portfolio-level market value, invested value, and profit/loss summary
- Trade transaction history with pagination and filters
- Global request validation and exception handling
- DTO-based API request and response models
- Swagger/OpenAPI documentation with JWT authorization
- Unit tests for core trade and stock service business logic

---

## Tech Stack

### Backend

- Java 17
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- Bean Validation
- JUnit 5
- Mockito
- Swagger / OpenAPI
- Maven

### Frontend

- React.js
- JavaScript
- HTML5
- CSS3

### Tools

- Git
- GitHub
- Postman
- Docker

---

## Backend Architecture

The backend is organized as a modular Spring Boot application.

```text
com.bhargav.titantrade
├── auth
├── user
├── wallet
├── stock
├── trade
├── portfolio
├── common
└── health
```

### Module Responsibilities

- `auth` — registration, login, JWT generation, authentication filter, and security configuration
- `user` — user entity, roles, gender, user status, and user-related domain models
- `wallet` — wallet balance management and wallet transaction records
- `stock` — stock master data, stock creation, price updates, stock search, and pagination
- `trade` — buy/sell stock execution and stock transaction history
- `portfolio` — portfolio holdings, market value, invested value, and unrealized profit/loss
- `common` — shared API response model, exception handling, security helpers, and OpenAPI configuration
- `health` — health check endpoint

---

## Core Business Flow

### Buy Stock Flow

When a user buys stock:

1. The backend validates that the stock exists.
2. The backend checks that the stock is active.
3. The current authenticated user is resolved from the JWT.
4. The stock execution price is taken from the stock’s latest known price.
5. The user wallet is debited.
6. The user portfolio holding is created or updated.
7. If the user already owns the stock, the weighted average buy price is recalculated.
8. A stock transaction record is created for audit/history.
9. The response returns the updated portfolio holding.

### Sell Stock Flow

When a user sells stock:

1. The backend validates that the stock exists.
2. The current authenticated user is resolved from the JWT.
3. The backend checks that the user owns the stock.
4. The backend verifies that the user has enough quantity to sell.
5. The portfolio holding quantity is reduced.
6. The user wallet is credited with the sale amount.
7. A sell transaction record is created.
8. The response returns the updated holding.

---

## API Modules

### Authentication

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### Stocks

- `POST /api/v1/stocks`
- `GET /api/v1/stocks`
- `GET /api/v1/stocks/{stockId}`
- `GET /api/v1/stocks/search?ticker=AAPL`
- `PUT /api/v1/stocks/{stockId}/price`

### Wallet

- Wallet deposit
- Wallet withdrawal
- Wallet details
- Wallet transaction records

### Trades

- `POST /api/v1/trades/buy`
- `POST /api/v1/trades/sell`
- `GET /api/v1/trades`
- `GET /api/v1/trades?stockId={stockId}`
- `GET /api/v1/trades?tradeType=BUY`
- `GET /api/v1/trades?page=0&size=10`

### Portfolio

- `GET /api/v1/portfolio`

---

## Security

TitanTrade uses Spring Security with JWT-based authentication.

Security features include:

- Password hashing using BCrypt
- JWT authentication filter
- Current authenticated user resolution
- Role-based access control
- Admin-only stock management APIs
- Customer access to authenticated trading and portfolio APIs

### Roles

```text
CUSTOMER
ADMIN
```

### Admin-Protected APIs

Only users with the `ADMIN` role can access:

```text
POST /api/v1/stocks
PUT /api/v1/stocks/{stockId}/price
```

---

## Pagination and Filtering

TitanTrade uses Spring Data `Pageable` for scalable APIs.

### Trade History

Trade history supports:

- Pagination
- Sorting by execution time descending
- Filter by stock
- Filter by trade type

Examples:

```text
GET /api/v1/trades?page=0&size=10
GET /api/v1/trades?tradeType=BUY&page=0&size=10
GET /api/v1/trades?stockId={stockId}&page=0&size=10
GET /api/v1/trades?stockId={stockId}&tradeType=SELL&page=0&size=10
```

### Stock Listing

Stock listing supports:

- Pagination
- Active stock filtering
- Search by ticker or company name
- Sorting by ticker ascending

Examples:

```text
GET /api/v1/stocks?page=0&size=10
GET /api/v1/stocks?search=apple&page=0&size=10
GET /api/v1/stocks?search=AAPL
```

---

## Validation and Exception Handling

TitanTrade uses Bean Validation and a global exception handler to return consistent API error responses.

Example validation response:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "stockId": "Stock id is required",
    "quantity": "Quantity must be greater than zero."
  }
}
```

Common handled exceptions include:

- Email already exists
- Login failed
- User not found
- Wallet not found
- Insufficient funds
- Stock not found
- Stock already exists
- Inactive stock
- Portfolio holding not found
- Insufficient holding quantity

---

## Swagger API Documentation

Swagger UI is available after starting the backend:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI docs are available at:

```text
http://localhost:8080/v3/api-docs
```

JWT-protected APIs can be tested from Swagger using the **Authorize** button.

---

## Testing

The backend includes unit tests using JUnit 5 and Mockito.

Covered areas include:

### Trade Service Tests

- Buy stock with new holding
- Buy stock with existing holding
- Weighted average buy price recalculation
- Inactive stock restriction
- Stock not found during buy
- Wallet failure during buy
- Sell stock success
- Sell stock when stock does not exist
- Sell stock when user does not own the stock
- Sell stock with insufficient holding quantity
- Trade history pagination
- Trade history filtering by stock and trade type

### Stock Service Tests

- Create stock successfully
- Duplicate ticker handling
- Get stock by ID
- Get stock by ticker
- Update stock price
- Paginated stock listing
- Page size cap validation

---

## Local Setup

### Prerequisites

- Java 17+
- Maven
- PostgreSQL
- Git

### Clone Repository

```bash
git clone https://github.com/snsbhargav/titantrade-platform.git
cd titantrade-platform/backend/titantrade
```

### Configure Database

Create a PostgreSQL database:

```sql
CREATE DATABASE titantrade;
```

Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/titantrade
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Run Backend

```bash
mvn spring-boot:run
```

Or using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

### Run Tests

```bash
mvn test
```

Or:

```bash
./mvnw test
```

---

## Sample API Response

```json
{
  "success": true,
  "message": "Stock bought successfully",
  "data": {
    "holdingId": "9f7a1f4e-7d5b-4b8d-91b6-2f6c4e9a0c10",
    "stockId": "7a9f6d4e-2b2d-4b1e-bf8f-0a8c24e12c32",
    "ticker": "AAPL",
    "companyName": "Apple",
    "quantity": 2,
    "averageBuyPrice": 190.00,
    "currentPrice": 190.00,
    "marketValue": 380.00,
    "investedValue": 380.00,
    "unrealizedProfitLoss": 0.00,
    "unrealizedProfitLossPercentage": 0.00
  }
}
```

---

## Future Enhancements

Planned improvements:

- React frontend dashboard
- Portfolio charts and visual analytics
- Real-time or external stock price integration
- More advanced stock filters using sector, exchange, currency, and asset type
- Case-insensitive enum query conversion
- Refresh token support
- Integration tests using Testcontainers
- Docker Compose setup for backend and PostgreSQL
- Deployment to cloud platform
- CI/CD pipeline using GitHub Actions

---

## Project Status

TitanTrade backend V1 is currently under active development.

Completed backend areas:

- Authentication
- Authorization
- Wallet
- Stock management
- Stock search and pagination
- Trade execution
- Portfolio analytics
- Trade history
- Global exception handling
- Swagger documentation
- Unit testing

Next major focus:

- React frontend integration
- Deployment
- README screenshots and demo flow

---

## Author

**Bhargav**  
Java Full Stack Developer  
GitHub: [snsbhargav](https://github.com/snsbhargav)
