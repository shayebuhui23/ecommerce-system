package com.ecommerce.vo;

import com.ecommerce.entity.Notification;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息响应。
 */
@Data
public class NotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private String link;
    private Integer isRead;
    private LocalDateTime createdAt;

    public static NotificationVO from(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setRelatedId(notification.getRelatedId());
        vo.setLink(notification.getLink());
        vo.setIsRead(notification.getIsRead());
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }
}
