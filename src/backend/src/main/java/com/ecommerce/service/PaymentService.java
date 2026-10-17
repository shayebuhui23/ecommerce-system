package com.ecommerce.service;

import com.ecommerce.common.PageResult;
import com.ecommerce.dto.PayDTO;
import com.ecommerce.vo.PaymentVO;

/**
 * 模拟支付服务。
 */
public interface PaymentService {

    /**
     * 发起模拟支付。成功时订单流转为待发货，失败时订单状态不变。
     */
    PaymentVO pay(PayDTO dto);

    /**
     * 查询指定订单的支付记录（客户只能查自己的订单）。
     */
    PaymentVO queryByOrder(Long orderId);

    /**
     * 管理员分页查询支付记录。
     */
    PageResult<PaymentVO> page(long pageNum, long pageSize, String keyword);
}
