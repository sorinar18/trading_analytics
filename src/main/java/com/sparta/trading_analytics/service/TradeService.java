package com.sparta.trading_analytics.service;

import com.sparta.trading_analytics.model.Trade;
import com.sparta.trading_analytics.model.TradeAggregationResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class TradeService {

    private final List<Trade> trades = new CopyOnWriteArrayList<>();

    public void addTrades(List<Trade> newTrades) {
        Set<String> batchSeen = new HashSet<>();

        for (Trade newTrade : newTrades) {
            // Create a unique key for the trade (all fields that define uniqueness)
            String key = newTrade.getCommodity() + "|" +
                    newTrade.getTraderId() + "|" +
                    newTrade.getTimestamp() + "|" +
                    newTrade.getPrice() + "|" +
                    newTrade.getQuantity();

            // Check for duplicates within the same batch
            if (!batchSeen.add(key)) {
                throw new IllegalArgumentException("Duplicate trade detected within submission batch for trader: "
                        + newTrade.getTraderId() + ", commodity: " + newTrade.getCommodity() + ", time: " + newTrade.getTimestamp());
            }

            // Check for duplicates against already stored trades
            boolean duplicateInStorage = trades.stream().anyMatch(existing ->
                    existing.getCommodity().equals(newTrade.getCommodity()) &&
                            existing.getTraderId().equals(newTrade.getTraderId()) &&
                            existing.getTimestamp().equals(newTrade.getTimestamp()) &&
                            existing.getPrice().equals(newTrade.getPrice()) &&
                            existing.getQuantity().equals(newTrade.getQuantity())
            );

            if (duplicateInStorage) {
                throw new IllegalArgumentException("Duplicate trade detected in storage for trader: "
                        + newTrade.getTraderId() + ", commodity: " + newTrade.getCommodity() + ", time: " + newTrade.getTimestamp());
            }
        }

        trades.addAll(newTrades);
    }


    public List<Trade> getAllTrades() {
        return new ArrayList<>(trades);
    }

    public Map<String, Object> getInsights() {
        if (trades.isEmpty()) {
            return buildEmptyInsightsResponse();
        }

        TradeAggregationResult result = aggregateTrades();
        Map<String, Double> averagePriceByCommodity = calculateAveragePrices(result);
        List<Map<String, Object>> allTradersByVolume = buildAllTradersList(result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalVolumeByCommodity", convertToIntegerMap(result.getTotalVolumeByCommodity()));
        response.put("averagePriceByCommodity", averagePriceByCommodity);
        response.put("topTradersByVolume", allTradersByVolume);
        return response;
    }

    // Aggregate all trades into a single result object
    private TradeAggregationResult aggregateTrades() {
        TradeAggregationResult result = new TradeAggregationResult();
        for (Trade trade : trades) {
            result.addTrade(trade);
        }
        return result;
    }

    // Calculate average prices
    private Map<String, Double> calculateAveragePrices(TradeAggregationResult result) {
        Map<String, Double> averagePrices = new HashMap<>();
        for (String commodity : result.getTotalPriceByCommodity().keySet()) {
            double totalPrice = result.getTotalPriceByCommodity().get(commodity);
            int count = result.getCountByCommodity().get(commodity);
            averagePrices.put(commodity, totalPrice / count);
        }
        return averagePrices;
    }

    // Helper to build full trader list
    private List<Map<String, Object>> buildAllTradersList(TradeAggregationResult result) {
        return result.getTraderVolumes().entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(entry -> {
                    Map<String, Object> traderMap = new HashMap<>();
                    traderMap.put("traderId", entry.getKey());
                    traderMap.put("volume", entry.getValue().intValue());
                    return traderMap;
                })
                .collect(Collectors.toList());
    }

    // Return an empty response map if no trades exist
    private Map<String, Object> buildEmptyInsightsResponse() {
        Map<String, Object> emptyResponse = new HashMap<>();
        emptyResponse.put("totalVolumeByCommodity", new HashMap<>());
        emptyResponse.put("averagePriceByCommodity", new HashMap<>());
        emptyResponse.put("topTradersByVolume", new ArrayList<>());
        return emptyResponse;
    }

    // Convert double map to integer map (for volumes)
    private Map<String, Integer> convertToIntegerMap(Map<String, Double> doubleMap) {
        return doubleMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().intValue()
                ));
    }
}
