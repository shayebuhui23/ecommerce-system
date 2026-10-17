package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 售后审核入参。
 */
@Data
public class AfterSaleAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择审核结果")
    private Boolean approved;

    @Size(max = 500, message = "审核意见不能超过 500 个字符")
    private String auditRemark;
}
