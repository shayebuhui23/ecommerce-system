package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发货与物流实体，映射 shipments 表。
 * <p>
 * 物流状态为模拟数据，不接入真实物流平台。
 */
@Data
@TableName("shipments")
public class Shipment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String shippingCompany;

    private String trackingNo;

    private String shippingMethod;

    private String fromCountry;

    private String toCountry;

    /** PENDING / IN_TRANSIT / DELIVERED */
    private String status;

    private LocalDate estimatedDelivery;

    private LocalDateTime shippedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
