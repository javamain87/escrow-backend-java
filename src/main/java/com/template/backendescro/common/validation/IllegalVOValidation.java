package com.template.backendescro.common.validation;

import java.lang.reflect.Field;

public class IllegalVOValidation {
    public static void validateNotNullOrEmpty(Object vo) {
        if (vo == null) {
            throw new IllegalArgumentException("입력 객체가 null입니다.");
        }

        Field[] fields = vo.getClass().getDeclaredFields();
        for (Field field : fields) {

            if (field.isAnnotationPresent(SkipValidation.class)) {
                continue; // ⛔ 유효성 검사 제외 필드는 스킵
            }

            field.setAccessible(true); // private 필드 접근 가능하게 설정
            try {
                Object value = field.get(vo);

                if (value == null) {
                    throw new IllegalArgumentException("필수값 누락: " + field.getName());
                }

                // 문자열인 경우 공백 체크
                if (value instanceof String && ((String) value).trim().isEmpty()) {
                    throw new IllegalArgumentException("필수값 누락 (빈 문자열): " + field.getName());
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("필드 접근 실패: " + field.getName(), e);
            }
        }
    }
}
