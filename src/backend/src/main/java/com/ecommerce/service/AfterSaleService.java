package com.ecommerce.service;

import com.ecommerce.common.PageResult;
import com.ecommerce.dto.AfterSaleApplyDTO;
import com.ecommerce.dto.AfterSaleAuditDTO;
import com.ecommerce.vo.AfterSaleVO;

/**
 * 售后与退款服务。
 */
public interface AfterSaleService {

    /**
     * 客户提交整单退款申请。
     */
    AfterSaleVO apply(AfterSaleApplyDTO dto);

    /**
     * 商家审核：通过则执行模拟退款并流转订单状态，驳回则仅更新售后单。
     */
    AfterSaleVO audit(Long id, AfterSaleAuditDTO dto);

    /**
     * 查询指定订单的售后单。
     */
    AfterSaleVO queryByOrder(Long orderId);

    /**
     * 分页查询售后单：客户看自己的，商家与管理员看全部。
     */
    PageResult<AfterSaleVO> page(long pageNum, long pageSize, String status);
}
