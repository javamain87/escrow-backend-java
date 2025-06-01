package com.template.backendescro.escrow.trade.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TradeHistoryVO {
    private Long id;
    private String tradeCode;
    private String writer; // seller or system
    private String content;
    private LocalDateTime createdAt;

    public TradeHistoryVO() {}

    public TradeHistoryVO(String tradeCode, String writer, String content) {
        this.tradeCode = tradeCode;
        this.writer = writer;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}