package com.template.backendescro.escrow.trade.controller;

import com.template.backendescro.escrow.trade.service.TradeService;
import com.template.backendescro.escrow.trade.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class TradeController {

        /** 전체 Flow
         * [1] 거래 생성 → [2] 판매자 승인 →
         * [3] 구매자 입금 (에스크로) →
         * [4] 판매자 배송 →
         * [5] 구매자 수령 확인 →
         * [6] 에스크로 → 판매자 송금
         */
    private final TradeService tradeService;


    // 1. 거래 등록 (구매자)
    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> register(@RequestBody TradeVO tradeVO) {
        Map<String, String> tradeMap = tradeService.registerTrade(tradeVO);
        return ResponseEntity.ok(tradeMap);
    }

    // 2. 비밀번호 설정 (판매자)
    @PostMapping("/set-password")
    public ResponseEntity<Map<String, String>> setSellerPassword(@RequestBody PasswordRequestVO request) {
        Map<String, String> tradeMap = tradeService.setApprovalPassword(request);
        return ResponseEntity.ok(tradeMap);
    }

    @PostMapping("/access") // 구매 상세 페이지 이동 시 구매자 / 판매자 체크 로직
    public ResponseEntity<?> accessTrade(@RequestBody TradeAccessRequestVO request) {
        boolean tradeChk = tradeService.validateAccess(request);
        if (!tradeChk) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("해당 거래의 사용자가 아닙니다.");
        }
        TradeVO trade = tradeService.getTradeByCode(request.getTradeCode());
        TradeDetailVO detail = tradeService.getTradeDetail(request.getTradeCode());
        return ResponseEntity.ok(new TradeAccessResponseVO(trade, detail));
    }


    @GetMapping("/status")
    public ResponseEntity<String> getStatus(@RequestParam String code) {
        return ResponseEntity.ok(tradeService.getTradeStatus(code));
    }

    // 상태 조회 - 거래 상태 조회
    // 판매자 승인 처리
    // 구매자 입금 확인 처리
    // 거래 조회 > 판매자: 패스워드 등록 여부  / 구매자: 거래 등록 여부    체크
    @PostMapping("/check-trade-code")
    public ResponseEntity<?> checkTradeCode(@RequestBody Map<String, String> request) {
        boolean returnVal =  tradeService.checkTradeCodeAndPassword(request);

        return ResponseEntity.ok(returnVal);
    }

    @PostMapping("/approve-seller")
    public ResponseEntity<Void> approveSeller(@RequestBody TradeStatusUpdateRequest req) {
        tradeService.updateTradeStatus(req.getTradeCode(), "SELLER_APPROVED");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<Void> confirmPayment(@RequestBody TradeStatusUpdateRequest req) {
        tradeService.updateTradeStatus(req.getTradeCode(), "BUYER_PAID");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update-status")
    public ResponseEntity<?> updateTradeStatus(@RequestBody TradeStatusUpdateRequest dto) {
        tradeService.updateStatus(dto);
        return ResponseEntity.ok("updated");
    }

    @GetMapping("/history")
    public ResponseEntity<List<TradeHistoryVO>> getTradeHistory(@RequestParam String tradeCode) {
        return ResponseEntity.ok(tradeService.getTradeHistory(tradeCode));
    }
}
