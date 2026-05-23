package com.pharmacy.admin.observer;

import org.springframework.context.ApplicationEvent;

public class AdminActionEvent extends ApplicationEvent {

    private final Long adminId;
    private final String action;
    private final String targetType;
    private final Long targetId;
    private final String detail;

    public AdminActionEvent(Object source, Long adminId, String action,
                            String targetType, Long targetId, String detail) {
        super(source);
        this.adminId = adminId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detail = detail;
    }

    public Long getAdminId() { return adminId; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getDetail() { return detail; }
}
