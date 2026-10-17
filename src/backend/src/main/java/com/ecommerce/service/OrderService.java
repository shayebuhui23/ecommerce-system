package com.ecommerce.service;

import com.ecommerce.common.PageResult;
import com.ecommerce.dto.OrderCreateDTO;
import com.ecommerce.vo.OrderDetailVO;
import com.ecommerce.vo.OrderVO;

/**
 * 订单服务。
 */
public interface OrderService {

    /**
     * 从购物车创建订单：校验库存、生成快照、扣减库存，全部在一个事务内完成。
     */
    OrderDetailVO create(OrderCreateDTO dto);

    /**
     * 分页查询订单。客户只能看到自己的订单，商家与管理员可查看全部。
     */
    PageResult<OrderVO> page(long pageNum, long pageSize, String status, String keyword);

    OrderDetailVO detail(Long id);

    /**
     * 取消订单并恢复库存（仅待付款订单可取消）。
     */
    void cancel(Long id);
}
