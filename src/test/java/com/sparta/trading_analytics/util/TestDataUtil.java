package com.sparta.trading_analytics.util;

import com.sparta.trading_analytics.model.Trade;

import java.time.Instant;

public class TestDataUtil {

    public static Trade createTrade(String commodity, String traderId, double price, int quantity, Instant timestamp) {
        Trade trade = new Trade();
        trade.setCommodity(commodity);
        trade.setTraderId(traderId);
        trade.setPrice(price);
        trade.setQuantity(quantity);
        trade.setTimestamp(timestamp);
        return trade;
    }
}
