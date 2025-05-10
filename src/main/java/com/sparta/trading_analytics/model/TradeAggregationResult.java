package com.sparta.trading_analytics.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class TradeAggregationResult {

    private final Map<String, Double> totalVolumeByCommodity = new HashMap<>();
    private final Map<String, Double> totalPriceByCommodity = new HashMap<>();
    private final Map<String, Integer> countByCommodity = new HashMap<>();
    private final Map<String, Double> traderVolumes = new HashMap<>();

    public void addTrade(Trade trade) {
        String commodity = trade.getCommodity();
        String traderId = trade.getTraderId();

        totalVolumeByCommodity.merge(commodity, trade.getQuantity().doubleValue(), Double::sum);
        totalPriceByCommodity.merge(commodity, trade.getPrice(), Double::sum);
        countByCommodity.merge(commodity, 1, Integer::sum);
        traderVolumes.merge(traderId, trade.getQuantity().doubleValue(), Double::sum);
    }
}
