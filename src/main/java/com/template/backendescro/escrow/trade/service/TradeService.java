package com.template.backendescro.escrow.trade.service;

import com.template.backendescro.common.utlis.PasswordUtil;
import com.template.backendescro.escrow.trade.mapper.TradeMapper;
import com.template.backendescro.escrow.trade.utils.TradeUtils;
import com.template.backendescro.escrow.trade.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final TradeUtils tradeUtils;
    private final TradeMapper tradeMapper;

    @Transactional
    public Map<String,String> registerTrade(TradeVO tradeVO) {
        TradeVO created = TradeVO.createWithGeneratedValues(tradeVO, baseUrl);
        try {
            log.info("Registering trade start: trade_code = {}, trade_link = {}", created.getTradeCode(), created.getLinkUrl());
            log.info("password (raw): {}", created.getTradePassword());

            Map<String, String> result = new HashMap<>();
            result.put("tradeCode", created.getTradeCode());
            result.put("password", created.getTradePassword());
            result.put("role", created.getRole());

            created.setTradePassword(PasswordUtil.hash(created.getTradePassword()));
            created.setStatus("PENDING");
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            created.setClientIp(request.getRemoteAddr());
            created.setUserAgent(request.getHeader("User-Agent"));
            created.setPaidAmount(Math.round(created.getAmount() * 0.95 * 100) / 100.0); // 소수점 2자리
            tradeMapper.register(created); // trade 저장

            // ✅ trade_detail 구매자 row 저장
            TradeDetailVO detail = TradeDetailVO.createWithGeneratedDetailValues(
                    created.getTradeCode(), created.getRole(), tradeVO.getRequesterName(), tradeVO.getRequesterEmail()
            );
            tradeMapper.insertTradeDetail(detail);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("거래 등록 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, String> setApprovalPassword(PasswordRequestVO request) {
        try {
            String hashed = PasswordUtil.hash(request.getApprovalPassword());
            TradeVO trade = tradeMapper.findTradeByCode(request.getTradeCode());

            if (trade != null) {
                TradeDetailVO sellerDetail = new TradeDetailVO(
                        hashed, "seller", trade.getTradeCode(), request.getName(), request.getEmail(),request.getPhone(), request.getCompany()
                );
                tradeMapper.insertSellerTradeDetail(sellerDetail);
            }

            Map<String, String> response = new HashMap<>();
            response.put("tradeCode", request.getTradeCode());
            response.put("password", request.getApprovalPassword());
            response.put("role", "seller");
            return response;
        } catch (Exception e) {
            throw new RuntimeException("판매자 승인 등록 중 오류 발생: " + e.getMessage());
        }
    }

    public TradeVO getTradeByCode(String tradeCode) {
        return tradeMapper.findTradeByCode(tradeCode);
    }

    public boolean validateAccess(TradeAccessRequestVO request) {
        boolean checkResult = false;
        try {
            if ("buyer".equalsIgnoreCase(request.getRole())) {
                TradeVO trade = new TradeVO();
                trade = tradeMapper.validateBuyerAccess(request.getTradeCode());
                return PasswordUtil.check(trade.getTradePassword(), request.getTradePassword());
            } else if ("seller".equalsIgnoreCase(request.getRole())) {
                TradeDetailVO tradeDetail = new TradeDetailVO();
                tradeDetail = tradeMapper.validateSellerAccess(request.getTradeCode());
                if (tradeDetail != null) {
                    checkResult = PasswordUtil.check(tradeDetail.getApprovalPassword(), request.getTradePassword());
                }

                if (tradeDetail == null) {
                    PasswordRequestVO passwordRequestVO = new PasswordRequestVO();
                    passwordRequestVO.setTradeCode(request.getTradeCode());
                    passwordRequestVO.setApprovalPassword(request.getTradePassword());
                    passwordRequestVO.setName(request.getName());
                    passwordRequestVO.setEmail(request.getEmail());
                    passwordRequestVO.setPhone(request.getPhone());
                    passwordRequestVO.setCompany(request.getCompany());
                    Map<String, String> result = this.setApprovalPassword(passwordRequestVO);
                    checkResult = !ObjectUtils.isEmpty(result);
                }
            }
            return checkResult;
        } catch (Exception e) {
            throw new RuntimeException("해당 거래의 사용자가 아닙니다.");
        }
    }

    public TradeDetailVO getTradeDetail(String tradeCode) {
        return tradeMapper.findTradeDetail(tradeCode);
    }

    public void updateTradeStatus(String tradeCode, String newStatus) {
        tradeMapper.updateTradeStatus(tradeCode, newStatus);

        if ("BUYER_PAID".equals(newStatus)) {
            tradeMapper.updatePaidAt(tradeCode);  // paid_at + paid_amount
        } else if ("SELLER_APPROVED".equals(newStatus)) {
            tradeMapper.updateConfirmedAt(tradeCode);
        } else if ("COMPLETED".equals(newStatus)) {
            tradeMapper.updateSettledAt(tradeCode); // settled_at + is_settled
        }

        // ✅ 자동 COMPLETED 처리
        TradeVO trade = tradeMapper.findByTradeCode(tradeCode);
        if ("SELLER_APPROVED".equals(trade.getStatus()) && Boolean.TRUE.equals(trade.getIsPaid())) {
            tradeMapper.updateTradeStatus(tradeCode, "COMPLETED");
        }
    }

    public void updateStatus(TradeStatusUpdateRequest dto) {
        tradeMapper.updateTradeStatus(dto.getTradeCode(), dto.getStatus());

        if ("seller".equals(dto.getRole())) {
            // 1. 배송 상태 및 최근 특이사항은 trade_detail에도 반영
            tradeMapper.updateTradeDetail(dto.getTradeCode(), dto.getDeliveryStatus(), dto.getSpecialNote());

            // 2. 히스토리 테이블에 기록
            if (dto.getSpecialNote() != null && !dto.getSpecialNote().isBlank()) {
                tradeMapper.insertTradeWorkHistory(dto.getTradeCode(), dto.getSpecialNote());
            }
        }

        // ✅ 자동 COMPLETED 로직
        if ("SELLER_APPROVED".equals(dto.getStatus()) || "BUYER_PAID".equals(dto.getStatus())) {
            TradeVO trade = tradeMapper.findByTradeCode(dto.getTradeCode());

            if ("SELLER_APPROVED".equals(trade.getStatus()) && Boolean.TRUE.equals(trade.getIsPaid())) {
                tradeMapper.updateTradeStatus(dto.getTradeCode(), "COMPLETED");
            }
        }
    }

    public String getTradeStatus(String tradeCode) {
        return tradeMapper.getTradeStatus(tradeCode);
    }

    public void passwordConfirm(String tradeCode , String password) {
        TradeVO vo = tradeMapper.findTradeByCode(tradeCode);
        boolean checkResult =  PasswordUtil.check(vo.getTradePassword(), password);

         if(checkResult) {
             log.info("same password");
         } else {
             log.info("different password");
         }
    }

    public boolean checkTradeCodeAndPassword(Map<String, String> request) {
        /*
        *   role check ( sellrer , buyer )
        *  1. buyer 일때
        *   -- > trade table 에서 trade_code / role 로 조회 후 있으면 true / 없으면 false
        *   --> 없으면 throw new IllgalException 처리
        *  2. seller 일 때
        *   --> trade_detail 에서 trade_code / role 로 조회 하여  있으면 true / 없으면 false
        *   --> 없을 때 false 값을 프론트에 전달하여 setApprovalPassword 전달 하여 패스워드 등록 api 로 연결
        **/
        boolean result = false;

        TradeVO trade = new TradeVO();
        trade = tradeMapper.validateBuyerAccess(request.get("tradeCode"));
        if (trade != null && "buyer".equalsIgnoreCase(request.get("role"))) {
            trade.setTradeCode(request.get("tradeCode"));
            result = trade != null;
        }

        if ("seller".equalsIgnoreCase(request.get("role"))) {
            if(trade == null) {
                throw new NullPointerException("데이터가 존재 하지 않습니다.");
            }
            TradeDetailVO tradeDetail = new TradeDetailVO();
            tradeDetail = tradeMapper.validateSellerAccess(request.get("tradeCode"));
            result =   tradeDetail != null && tradeDetail.getApprovalPassword() != null && !tradeDetail.getApprovalPassword().isEmpty();
        }
        return result;
    }

    public List<TradeHistoryVO> getTradeHistory(String tradeCode) {
        return tradeMapper.selectHistoryByTradeCode(tradeCode);
    }
}
