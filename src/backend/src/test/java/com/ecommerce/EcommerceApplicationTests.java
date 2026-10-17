package com.ecommerce;

import com.ecommerce.common.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 骨架阶段的基础测试：验证工程可加载、统一响应结构可用、密码哈希可校验。
 */
@SpringBootTest
class EcommerceApplicationTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        // 上下文加载成功即通过
    }

    @Test
    void successResultShouldUseUnifiedStructure() {
        Result<String> result = Result.success("ok");
        assertEquals(200, result.getCode());
        assertEquals("ok", result.getData());
    }

    @Test
    void bcryptShouldMatchRawPassword() {
        String hash = passwordEncoder.encode("123456");
        assertTrue(passwordEncoder.matches("123456", hash));
        assertTrue(!passwordEncoder.matches("654321", hash));
    }
}
