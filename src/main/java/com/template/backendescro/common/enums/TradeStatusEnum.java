package com.template.backendescro.common.enums;

public enum TradeStatusEnum {
        PENDING,            // 거래 생성 직후
        SELLER_APPROVED,    // 판매자 승인 완료
        BUYER_PAID,         // 구매자 입금 완료
        COMPLETED,          // 승인 + 입금 모두 완료
        CANCELLED           // 거래 취소 (선택적)
}
