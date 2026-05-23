package com.pharmacy.admin.observer;

import com.pharmacy.admin.entity.AuditLog;
import com.pharmacy.admin.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditLogListener {

    private static final Logger log = LoggerFactory.getLogger(AuditLogListener.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @EventListener(AdminActionEvent.class)
    @Async
    public void handle(AdminActionEvent event) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAdminId(event.getAdminId());
            auditLog.setAction(event.getAction());
            auditLog.setTargetType(event.getTargetType());
            auditLog.setTargetId(event.getTargetId());
            auditLog.setDetail(event.getDetail());
            auditLogRepository.save(auditLog);
            log.debug("AuditLog persisted — action={} targetType={} targetId={}",
                    event.getAction(), event.getTargetType(), event.getTargetId());
        } catch (Exception ex) {
            log.error("Failed to persist AuditLog for action={} adminId={}",
                    event.getAction(), event.getAdminId(), ex);
        }
    }
}
