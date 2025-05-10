package com.sparta.trading_analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(
        description = "Trade details",
        example = "{ \"commodity\": \"Gold\", \"traderId\": \"T001\", \"price\": 2000.0, \"quantity\": 50, \"timestamp\": \"2025-05-10T10:00:00Z\" }"
)
public class Trade {

    @NotNull(message = "Commodity is required")
    private String commodity;

    @NotNull(message = "Trader ID is required")
    private String traderId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Timestamp is required")
    @PastOrPresent(message = "Timestamp cannot be in the future")
    private Instant timestamp;
}
