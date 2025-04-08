package org.example.plain.common.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Message {

    OK("정상적으로 처리되었습니다."),
    UNAUTHORIZES("로그인이 필요합니다.");
    private final String message;
}
