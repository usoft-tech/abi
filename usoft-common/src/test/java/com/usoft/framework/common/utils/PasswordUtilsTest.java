package com.usoft.framework.common.utils;

import com.usoft.framework.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordUtilsTest {

    @Test
    void validatePasswordAcceptsStrongPassword() {
        assertDoesNotThrow(() -> PasswordUtils.validatePassword("Secur3!Pass2026", "alice"));
    }

    @Test
    void validatePasswordRejectsShortPassword() {
        BizException exception = assertThrows(BizException.class,
                () -> PasswordUtils.validatePassword("Aa1!", "alice"));

        assertEquals("密码长度不能小于8位", exception.getMessage());
    }

    @Test
    void validatePasswordRejectsInsufficientCharacterTypes() {
        BizException exception = assertThrows(BizException.class,
                () -> PasswordUtils.validatePassword("longpassword", "alice"));

        assertEquals("密码必须包含至少3种字符类型（大写字母、小写字母、数字、特殊字符）", exception.getMessage());
    }

    @Test
    void validatePasswordRejectsPasswordContainingUsername() {
        BizException exception = assertThrows(BizException.class,
                () -> PasswordUtils.validatePassword("Alice2026!Strong", "alice"));

        assertEquals("密码不能包含用户名", exception.getMessage());
    }

    @Test
    void validatePasswordRejectsPasswordContainingReversedUsername() {
        BizException exception = assertThrows(BizException.class,
                () -> PasswordUtils.validatePassword("Ecila2026!Strong", "alice"));

        assertEquals("密码不能包含用户名的倒序", exception.getMessage());
    }
}
