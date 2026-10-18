package com.ecommerce.service;

import com.ecommerce.vo.NotificationVO;

import java.util.Collection;
import java.util.List;

/**
 * 站内消息服务。
 */
public interface NotificationService {

    /**
     * 客户催发货：给订单涉及的商家发送提醒。
     */
    void notifyOrderUrge(Collection<Long> sellerIds, Long orderId, String orderNo, String customerName);

    /**
     * 买家已寄回退货商品，提醒商家收货。
     */
    void notifyReturnShipped(Collection<Long> sellerIds, Long orderId, String orderNo);

    /**
     * 当前登录用户的通知列表。
     */
    List<NotificationVO> list(boolean unreadOnly, int limit);

    /**
     * 当前登录用户的未读数量。
     */
    long unreadCount();

    /**
     * 将当前用户的一条通知标记为已读。
     */
    void markRead(Long id);

    /**
     * 将当前用户的全部通知标记为已读。
     */
    void markAllRead();
}
