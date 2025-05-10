# Trading Analytics API

A simple REST API for submitting and analyzing commodity trade data.

---

## 🚀 Setup & Run Instructions

### 1️⃣ Clone the repository

```bash

git clone https://github.com/sorinar18/trading_analytics.git
cd trading_analytics
```
### 2️⃣ Run with Maven
```bash

./mvnw clean install
./mvnw spring-boot:run
```
- API will be available at: http://localhost:8080
### 3️⃣ Run with Docker (optional)
```bash

docker build -t trading_analytics.
docker run -p 8080:8080 trading_analytics
```
## 📖 API Documentation
Swagger UI is available at:
```bash

http://localhost:8080/swagger-ui.html
```
Example payload (POST /trades):
```json
[
  {
    "commodity": "Gold",
    "traderId": "T001",
    "price": 2000.0,
    "quantity": 50,
    "timestamp": "2025-05-10T10:00:00Z"
  }
]
```
## 🛠️ Dependencies
- Java 17

- Spring Boot 3.x

- Spring Web

- Spring Validation

- Springdoc OpenAPI (Swagger)

- JUnit & MockMvc (for testing)

- Docker (optional)

## 💡 Assumptions / Notes
- Trades are unique by combination of traderId and timestamp. Submitting a duplicate trade will result in a 400 Bad Request.

- All data is stored in-memory only. Data will be lost when the application restarts.

- Error responses are standardized:
```json
{
  "success": false,
  "errors": {
    "price": "Price must be positive",
    "quantity": "Quantity must be positive"
  }
}

```
- Success responses return plain strings (no wrapping).

## ✅ Tests
Run all tests with:
```bash

./mvnw test
```
Includes:
- Unit tests for service logic
- Integration tests for full API behavior

## ✍️ Additional Notes
- No authentication is implemented, as the spec did not require it.
- The API is kept simple & clean, focusing on functionality and readability.