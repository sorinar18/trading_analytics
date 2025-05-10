package com.sparta.trading_analytics.service;

import com.sparta.trading_analytics.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static com.sparta.trading_analytics.util.TestDataUtil.createTrade;


import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TradeServiceTests {

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        tradeService = new TradeService();
    }

    @Test
    void testAddTrades_Success() {
        Trade trade = createTrade("Gold", "T001", 2000.0, 50, Instant.now());
        tradeService.addTrades(List.of(trade));

        List<Trade> allTrades = tradeService.getAllTrades();
        assertEquals(1, allTrades.size());
        assertEquals("Gold", allTrades.get(0).getCommodity());
    }

    @Test
    void testAddTrades_Duplicate_ThrowsException() {
        Trade trade1 = createTrade("Oil", "T002", 85.5, 100, Instant.now());
        Trade trade2 = createTrade("Oil", "T002", 85.5, 100, trade1.getTimestamp());  // Same as trade1

        tradeService.addTrades(List.of(trade1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.addTrades(List.of(trade2));
        });

        assertTrue(exception.getMessage().contains("Duplicate trade detected"));
    }

    @Test
    void testGetAllTrades() {
        Trade trade1 = createTrade("Copper", "T003", 4.5, 200, Instant.now());
        Trade trade2 = createTrade("Silver", "T004", 25.0, 150, Instant.now());

        tradeService.addTrades(List.of(trade1, trade2));

        List<Trade> allTrades = tradeService.getAllTrades();
        assertEquals(2, allTrades.size());
    }

    @Test
    void testGetInsights_NoTrades() {
        Map<String, Object> insights = tradeService.getInsights();

        assertTrue(((Map<?, ?>) insights.get("totalVolumeByCommodity")).isEmpty());
        assertTrue(((Map<?, ?>) insights.get("averagePriceByCommodity")).isEmpty());
        assertTrue(((List<?>) insights.get("topTradersByVolume")).isEmpty());
    }

    @Test
    void testGetInsights_WithTrades() {
        Trade trade1 = createTrade("Gold", "T001", 2000.0, 50, Instant.now());
        Trade trade2 = createTrade("Gold", "T002", 2100.0, 30, Instant.now());
        Trade trade3 = createTrade("Silver", "T001", 25.0, 100, Instant.now());

        tradeService.addTrades(List.of(trade1, trade2, trade3));

        Map<String, Object> insights = tradeService.getInsights();

        Map<String, Integer> totalVolume = (Map<String, Integer>) insights.get("totalVolumeByCommodity");
        Map<String, Double> averagePrice = (Map<String, Double>) insights.get("averagePriceByCommodity");
        List<Map<String, Object>> tradersByVolume = (List<Map<String, Object>>) insights.get("topTradersByVolume");

        assertEquals(2, totalVolume.size());
        assertEquals(80, totalVolume.get("Gold"));
        assertEquals(100, totalVolume.get("Silver"));

        assertEquals(2, averagePrice.size());
        assertEquals(2050.0, averagePrice.get("Gold"));
        assertEquals(25.0, averagePrice.get("Silver"));

        assertEquals(2, tradersByVolume.size());
        assertEquals("T001", tradersByVolume.get(0).get("traderId"));
        assertEquals(150, tradersByVolume.get(0).get("volume"));
    }

    @Test
    void testAddTrades_MultipleWithDuplicate_ThrowsException() {
        Trade trade1 = createTrade("Oil", "T002", 85.5, 100, Instant.now());
        Trade trade2 = createTrade("Oil", "T002", 85.5, 100, trade1.getTimestamp());  // Duplicate of trade1
        Trade trade3 = createTrade("Gold", "T003", 2000.0, 50, Instant.now());  // New trade

        tradeService.addTrades(List.of(trade1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.addTrades(List.of(trade2, trade3));  // One duplicate, one new
        });

        assertTrue(exception.getMessage().contains("Duplicate trade detected"));
    }

    @Test
    void testGetInsights_SameTraderMultipleTrades_SumsVolume() {
        Trade trade1 = createTrade("Gold", "T001", 2000.0, 50, Instant.now());
        Trade trade2 = createTrade("Silver", "T001", 25.0, 100, Instant.now());

        tradeService.addTrades(List.of(trade1, trade2));

        Map<String, Object> insights = tradeService.getInsights();
        List<Map<String, Object>> topTraders = (List<Map<String, Object>>) insights.get("topTradersByVolume");

        assertEquals(1, topTraders.size());
        assertEquals("T001", topTraders.get(0).get("traderId"));
        assertEquals(150, topTraders.get(0).get("volume"));  // 50 + 100
    }

    @Test
    void testAddTrades_SameTraderDifferentTimestamps_Success() {
        Trade trade1 = createTrade("Gold", "T001", 2000.0, 50, Instant.now());
        Trade trade2 = createTrade("Gold", "T001", 2000.0, 50, Instant.now().plusSeconds(60));  // Different timestamp

        tradeService.addTrades(List.of(trade1, trade2));

        List<Trade> allTrades = tradeService.getAllTrades();
        assertEquals(2, allTrades.size());
    }

}
