package com.template.backendescro.escrow.trade.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TradeDetailVO {
    private Long id;
    private String tradeCode;
    private String role; // seller or buyer
    private String approvalPassword;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String deliveryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TradeDetailVO() {
        // 기본 생성자
    }

    public TradeDetailVO(String hashed, String role, String tradeCode, String name, String email, String phone, String company) {
        if (tradeCode == null) throw new IllegalArgumentException("tradeCode is null");
        if (role == null) throw new IllegalArgumentException("role is null");

        this.tradeCode = tradeCode;
        this.role = role;
        this.approvalPassword = hashed;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    // ✅ 이름과 전화번호까지 포함한 오버로드 메서드
    public static TradeDetailVO createWithGeneratedDetailValues(String tradeCode, String role, String name, String email) {
        if (tradeCode == null || role == null) throw new IllegalArgumentException("tradeCode or role is null");

        TradeDetailVO tradeDetailVO = new TradeDetailVO();
        tradeDetailVO.tradeCode = tradeCode;
        tradeDetailVO.role = role;
        tradeDetailVO.name = name;
        tradeDetailVO.email = email;
        tradeDetailVO.company = "";

        return tradeDetailVO;
    }

    // 판매자용 패스워드 포함 생성자
    public TradeDetailVO(String approvalPassword, String role, String tradeCode) {
        this.approvalPassword = approvalPassword;
        this.role = role;
        this.tradeCode = tradeCode;
    }
}
