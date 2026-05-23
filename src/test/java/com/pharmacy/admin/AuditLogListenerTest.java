package com.pharmacy.admin;

import com.pharmacy.admin.entity.AuditLog;
import com.pharmacy.admin.observer.AdminActionEvent;
import com.pharmacy.admin.observer.AuditLogListener;
import com.pharmacy.admin.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test for AuditLogListener.
 * Note: @Async is bypassed when calling handle() directly — this is intentional for unit testing.
 * Async behavior is verified in the integration tests.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogListenerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogListener auditLogListener;

    @Test
    void handle_shouldPersistAuditLogWithMatchingFields() {
        AdminActionEvent event = new AdminActionEvent(
                this,
                42L,
                "TOGGLE_STATUS",
                "PHARMACY",
                7L,
                "active=false"
        );

        auditLogListener.handle(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getAdminId()).isEqualTo(42L);
        assertThat(saved.getAction()).isEqualTo("TOGGLE_STATUS");
        assertThat(saved.getTargetType()).isEqualTo("PHARMACY");
        assertThat(saved.getTargetId()).isEqualTo(7L);
        assertThat(saved.getDetail()).isEqualTo("active=false");
    }

    @Test
    void handle_shouldNotPropagateException_whenRepositoryThrows() {
        AdminActionEvent event = new AdminActionEvent(
                this, 1L, "TOGGLE_STATUS", "PHARMACY", 1L, "active=true"
        );
        doThrow(new RuntimeException("DB error")).when(auditLogRepository).save(any());

        // Must not throw — exception is caught and logged inside handle()
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> auditLogListener.handle(event)
        );
    }
}
