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
 * 客户收货地址实体，映射 addresses 表。
 * <p>
 * 字段按国际地址的常见结构设计，不依赖中国大陆固定行政区划。
 */
@Data
@TableName("addresses")
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String consignee;

    private String country;

    private String provinceState;

    private String city;

    private String postalCode;

    private String addressLine;

    private String phone;

    /** 1 默认地址，0 普通地址 */
    private Integer isDefault;

    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
