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
 * 订单商品快照实体，映射 order_items 表。
 * <p>
 * 商品名称、SKU、单价、图片均在下单时固化，商品后续改价不影响历史订单。
 */
@Data
@TableName("order_items")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    private Long skuId;

    private String productName;

    private String productNameEn;

    private String skuCode;

    private String specName;

    private String imageUrl;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal subtotal;

    private String currency;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
