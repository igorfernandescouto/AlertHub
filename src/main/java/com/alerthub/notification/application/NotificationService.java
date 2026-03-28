package com.alerthub.notification.application;

import java.util.UUID;

import com.alerthub.notification.api.dto.NotificationResponse;
import com.alerthub.notification.domain.NotificationChannel;
import com.alerthub.notification.domain.NotificationStatus;
import com.alerthub.notification.infrastructure.NotificationRecordRepository;
import com.alerthub.shared.paging.PagedResponse;
import com.alerthub.shared.security.TenantAccessVerifier;
import com.alerthub.user.domain.UserRole;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationService {

    private final NotificationRecordRepository notificationRecordRepository;
    private final TenantAccessVerifier tenantAccessVerifier;

    public NotificationService(
            NotificationRecordRepository notificationRecordRepository,
            TenantAccessVerifier tenantAccessVerifier
    ) {
        this.notificationRecordRepository = notificationRecordRepository;
        this.tenantAccessVerifier = tenantAccessVerifier;
    }

    public PagedResponse<NotificationResponse> search(NotificationStatus status, NotificationChannel channel, int page, int size) {
        UUID tenantId = tenantAccessVerifier.currentTenantId();
        tenantAccessVerifier.requireTenantRole(tenantId, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER);

        var query = notificationRecordRepository.search(tenantId, status, channel, page, size);

        return PagedResponse.of(
                query.list().stream().map(NotificationResponse::from).toList(),
                page,
                size,
                query.count()
        );
    }
}
