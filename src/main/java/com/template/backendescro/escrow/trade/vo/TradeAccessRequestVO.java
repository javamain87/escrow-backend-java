package com.template.backendescro.escrow.trade.vo;

import lombok.Data;

@Data
public class TradeAccessRequestVO extends TradeDetailVO{
    private String tradeCode;
    private String tradePassword;
    private String role; // "seller" or "buyer"
}
