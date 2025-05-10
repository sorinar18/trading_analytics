package com.sparta.trading_analytics.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.trading_analytics.model.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static com.sparta.trading_analytics.util.TestDataUtil.createTrade;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TradeControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPostTrades_Success() throws Exception {
        Trade trade = createTrade("Gold", "T001", 2000.0, 50, Instant.now());

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(trade))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trades added successfully")));
    }

    @Test
    void testPostTrades_ValidationError() throws Exception {
        Trade invalidTrade = new Trade();  // Missing all required fields

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(invalidTrade))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.commodity").value("Commodity is required"))
                .andExpect(jsonPath("$.errors.traderId").value("Trader ID is required"))
                .andExpect(jsonPath("$.errors.price").value("Price is required"))
                .andExpect(jsonPath("$.errors.quantity").value("Quantity is required"))
                .andExpect(jsonPath("$.errors.timestamp").value("Timestamp is required"));
    }

    @Test
    void testPostTrades_MalformedJson() throws Exception {
        String malformedJson = "[{\"commodity\": \"Gold\", \"price\": 2000";  // Broken JSON

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.error", containsString("Malformed JSON")));
    }

    @Test
    void testPostTrades_DuplicateTrade() throws Exception {
        Trade trade = createTrade("Oil", "T002", 85.0, 100, Instant.now());

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(trade))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(trade))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.error", containsString("Duplicate trade detected")));
    }

    @Test
    void testGetInsights_NoTrades() throws Exception {
        mockMvc.perform(get("/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVolumeByCommodity").isEmpty())
                .andExpect(jsonPath("$.averagePriceByCommodity").isEmpty())
                .andExpect(jsonPath("$.topTradersByVolume").isEmpty());
    }

    @Test
    void testGetInsights_WithTrades() throws Exception {
        Trade trade1 = createTrade("Gold", "T001", 2000.0, 50, Instant.now());
        Trade trade2 = createTrade("Silver", "T002", 25.0, 100, Instant.now());

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(trade1, trade2))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVolumeByCommodity.Gold").value(50))
                .andExpect(jsonPath("$.totalVolumeByCommodity.Silver").value(100))
                .andExpect(jsonPath("$.averagePriceByCommodity.Gold").value(2000.0))
                .andExpect(jsonPath("$.averagePriceByCommodity.Silver").value(25.0))
                .andExpect(jsonPath("$.topTradersByVolume[0].traderId", anyOf(is("T001"), is("T002"))))
                .andExpect(jsonPath("$.topTradersByVolume", hasSize(2)));
    }

}
