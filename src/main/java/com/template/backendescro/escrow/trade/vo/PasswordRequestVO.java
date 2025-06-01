package com.template.backendescro.escrow.trade.vo;

import lombok.Data;

@Data
public class PasswordRequestVO {
    private String tradeCode;
    private String approvalPassword; // 사용자가 설정하려는 비밀번호
    private String name;
    private String email;
    private String phone;
    private String company;

}
