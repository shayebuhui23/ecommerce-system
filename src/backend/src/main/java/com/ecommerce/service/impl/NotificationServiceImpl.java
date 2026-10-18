package com.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.entity.Notification;
import com.ecommerce.mapper.NotificationMapper;
import com.ecommerce.security.LoginUserUtil;
import com.ecommerce.service.NotificationService;
import com.ecommerce.vo.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 站内消息实现。
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final String TYPE_ORDER_URGE = "ORDER_URGE";
    private static final String TYPE_RETURN_SHIPPED = "RETURN_SHIPPED";
    private static final int MAX_LIST_SIZE = 50;

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public void notifyOrderUrge(Collection<Long> sellerIds, Long orderId, String orderNo, String customerName) {
        String content = "客户 " + customerName + " 提醒您尽快发货，订单号 " + orderNo;
        notifySellers(sellerIds, TYPE_ORDER_URGE, "客户催发货", content, orderId, "/admin/orders");
        log.info("已发送催发货提醒：orderNo={}, sellers={}", orderNo, sellerIds);
    }

    @Override
    public void notifyReturnShipped(Collection<Long> sellerIds, Long orderId, String orderNo) {
        String content = "买家已寄回退货商品，请及时确认收货，订单号 " + orderNo;
        notifySellers(sellerIds, TYPE_RETURN_SHIPPED, "退货待收货", content, orderId, "/admin/after-sales");
        log.info("已发送退货收货提醒：orderNo={}, sellers={}", orderNo, sellerIds);
    }

    private void notifySellers(Collection<Long> sellerIds, String type, String title,
                               String content, Long relatedId, String link) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return;
        }
        for (Long sellerId : sellerIds) {
            Notification notification = new Notification();
            notification.setUserId(sellerId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedId(relatedId);
            notification.setLink(link);
            notification.setIsRead(0);
            notification.setDeleted(0);
            notificationMapper.insert(notification);
        }
    }

    @Override
    public List<NotificationVO> list(boolean unreadOnly, int limit) {
        Long userId = LoginUserUtil.currentUserId();
        int size = Math.min(Math.max(limit, 1), MAX_LIST_SIZE);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .last("LIMIT " + size);
        if (unreadOnly) {
            wrapper.eq(Notification::getIsRead, 0);
        }
        return notificationMapper.selectList(wrapper).stream()
                .map(NotificationVO::from)
                .collect(Collectors.toList());
    }

    @Override
    public long unreadCount() {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, LoginUserUtil.currentUserId())
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markRead(Long id) {
        Notification update = new Notification();
        update.setIsRead(1);
        notificationMapper.update(update, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, LoginUserUtil.currentUserId()));
    }

    @Override
    public void markAllRead() {
        Notification update = new Notification();
        update.setIsRead(1);
        notificationMapper.update(update, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, LoginUserUtil.currentUserId())
                .eq(Notification::getIsRead, 0));
    }
}
