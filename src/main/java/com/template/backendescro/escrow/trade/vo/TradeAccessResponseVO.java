package com.template.backendescro.escrow.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeAccessResponseVO {
    private TradeVO trade;
    private TradeDetailVO tradeDetail;
}
