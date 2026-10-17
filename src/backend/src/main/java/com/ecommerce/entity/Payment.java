package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟支付记录实体，映射 payments 表。
 * <p>
 * 本项目不接入真实支付网关，只记录模拟支付的结果，供订单状态流转与后台查询使用。
 */
@Data
@TableName("payments")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String paymentNo;

    private Long orderId;

    private Long userId;

    private BigDecimal amount;

    private String currency;

    /** 支付方式，本项目固定为 MOCK */
    private String method;

    /** 1 成功，0 失败 */
    private Integer status;

    private String failReason;

    private String tradeNo;

    private LocalDateTime paidAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
