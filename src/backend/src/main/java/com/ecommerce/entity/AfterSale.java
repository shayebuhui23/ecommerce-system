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
 * 售后与退款申请实体，映射 after_sales 表。
 * <p>
 * 首版采用整单退款模型：一笔订单同时只能存在一条进行中的售后申请。
 */
@Data
@TableName("after_sales")
public class AfterSale implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String afterSaleNo;

    private Long orderId;

    private Long userId;

    /** REFUND 整单退款 */
    private String type;

    private String reason;

    private String description;

    private BigDecimal amount;

    private String currency;

    /** PENDING / APPROVED / REJECTED / REFUNDED */
    private String status;

    private String auditRemark;

    private Long auditedBy;

    private LocalDateTime auditedAt;

    private LocalDateTime refundedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
