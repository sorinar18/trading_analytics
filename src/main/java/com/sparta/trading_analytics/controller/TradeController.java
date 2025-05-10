package com.sparta.trading_analytics.controller;

import com.sparta.trading_analytics.model.Trade;
import com.sparta.trading_analytics.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/trades")
    public ResponseEntity<String> addTrades(@RequestBody @Valid List<Trade> trades) {
        if (trades.isEmpty()) {
            throw new IllegalArgumentException("Trade list cannot be empty");
        }
        tradeService.addTrades(trades);
        return ResponseEntity.ok("Trades added successfully");
    }


    @GetMapping("/trades")
    public List<Trade> getAllTrades() {
        return tradeService.getAllTrades();
    }

    @GetMapping("/insights")
    public Map<String, Object> getInsights() {
        return tradeService.getInsights();
    }

}

