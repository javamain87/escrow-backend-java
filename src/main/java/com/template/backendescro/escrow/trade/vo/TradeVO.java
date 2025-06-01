package com.template.backendescro.escrow.trade.vo;

import com.template.backendescro.common.validation.IllegalVOValidation;
import com.template.backendescro.common.validation.SkipValidation;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TradeVO {

        @SkipValidation
        private String tradeCode;

        @SkipValidation
        private String tradePassword;

        private String role;
        private String payment;
        private Double amount;
        private String description;
        private String deliveryTime;
        @SkipValidation
        private String phone;
        // 구매자 이름 / 전화번호 필드 추가
        private String requesterName;
        @SkipValidation
        private String requesterEmail;
        @SkipValidation
        private String clientIp;
        @SkipValidation
        private String userAgent;
        @SkipValidation
        private Double paidAmount;

        @SkipValidation
        private String linkUrl;

        @SkipValidation
        private Boolean isPaid;

        @SkipValidation
        private String status;

        public TradeVO() {
                // 기본 생성자
        }

        public static TradeVO createWithGeneratedValues(TradeVO tradeVO, String baseUrl) {
                if (tradeVO == null) {
                        throw new IllegalArgumentException("tradeVO is null");
                }
                if (baseUrl == null) {
                        throw new IllegalArgumentException("baseUrl is null");
                }

                IllegalVOValidation.validateNotNullOrEmpty(tradeVO);

                TradeVO result = new TradeVO();
                result.tradeCode = "id" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                result.tradePassword = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
                result.role = tradeVO.getRole();
                result.payment = tradeVO.getPayment();
                result.amount = tradeVO.getAmount();
                result.phone = tradeVO.getPhone();
                result.requesterEmail = tradeVO.getRequesterEmail();
                result.amount = tradeVO.getAmount();
                result.description = tradeVO.getDescription();
                result.deliveryTime = tradeVO.getDeliveryTime();
                result.linkUrl = baseUrl + result.tradeCode;
                result.isPaid = false;
                result.status = "PENDING";

                // ✅ 전달 받은 이름/전화번호 세팅
                result.requesterName = tradeVO.getRequesterName();
                result.requesterEmail = tradeVO.getRequesterEmail();

                return result;
        }
}
