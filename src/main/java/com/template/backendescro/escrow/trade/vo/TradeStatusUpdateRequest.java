package com.template.backendescro.escrow.trade.vo;

import lombok.Data;

@Data
public class TradeStatusUpdateRequest {
    private String tradeCode;
    private String role;               // seller or buyer
    private String status;            // eg. BUYER_PAID, DELIVERED
    private String deliveryStatus;    // eg. SHIPPING
    private String specialNote;
}
