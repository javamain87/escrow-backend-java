package com.template.backendescro.escrow.trade.mapper;

import com.template.backendescro.escrow.trade.vo.TradeDetailVO;
import com.template.backendescro.escrow.trade.vo.TradeHistoryVO;
import com.template.backendescro.escrow.trade.vo.TradeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TradeMapper {
    void register(TradeVO registerVO); // trade 정보 생성
    void insertTradeDetail(TradeDetailVO tradeDetailVO); //trade detail 정보 생성


    void insertSellerTradeDetail(TradeDetailVO tradeDetailVO);
    void insertTradeWorkHistory(String tradeCode, String content);

    TradeVO findTradeByCode(String tradeCode);

    TradeVO findByTradeCode(String tradeCode);

    TradeDetailVO findTradeDetail(String tradeCode);

    String getTradeStatus(String tradeCode);

    TradeVO validateBuyerAccess(String tradeCode);

    TradeDetailVO validateSellerAccess(String tradeCode);


    void updateTradeStatus(String tradeCode,String status);

    void updateTradeDetail(String tradeCode,String deliveryStatus,String specialNote);

    List<TradeHistoryVO> selectHistoryByTradeCode(String tradeCode);

    void updatePaidAt(String tradeCode);

    void updateConfirmedAt(String tradeCode);

    void updateSettledAt(String tradeCode);
}
