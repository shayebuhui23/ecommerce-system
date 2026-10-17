package com.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 汇率调整入参。
 */
@Data
public class ExchangeRateUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "汇率不能为空")
    @DecimalMin(value = "0.000001", message = "汇率必须大于 0")
    private BigDecimal rate;
}
