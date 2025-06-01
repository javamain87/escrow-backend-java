package com.template.backendescro.escrow.trade.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TradeUtils {


    public String generateTradeCode() {
        return "id" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    public String generateTradePassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
}
