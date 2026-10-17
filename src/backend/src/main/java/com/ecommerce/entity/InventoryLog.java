package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存变更记录实体，映射 inventory_logs 表。
 * <p>
 * 每次扣减与恢复都会留痕，便于核对"同一订单不会重复恢复库存"。
 */
@Data
@TableName("inventory_logs")
public class InventoryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private Long skuId;

    private Long orderId;

    /** ORDER_DEDUCT / ORDER_CANCEL / REFUND_RESTORE / MANUAL_ADJUST */
    private String changeType;

    private Integer changeQuantity;

    private Integer beforeStock;

    private Integer afterStock;

    private String remark;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
