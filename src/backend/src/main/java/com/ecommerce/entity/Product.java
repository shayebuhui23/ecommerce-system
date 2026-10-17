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
 * 商品实体，映射 products 表。
 * <p>
 * 已产生订单的商品只做逻辑删除/下架，不做物理删除，保证历史数据可追溯。
 */
@Data
@TableName("products")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String skuCode;

    private String name;

    private String nameEn;

    private String description;

    private String descriptionEn;

    private String imageUrl;

    private BigDecimal price;

    private String currency;

    private Integer stock;

    private Integer salesCount;

    /** 1 上架，0 下架 */
    private Integer status;

    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
