CREATE TABLE users (
    id UUID NOT NULL,
    version BIGINT NOT NULL,
    tenant_id UUID NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uk_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE TABLE alert_rules (
    id UUID NOT NULL,
    version BIGINT NOT NULL,
    tenant_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    payload_field VARCHAR(160),
    operator VARCHAR(32) NOT NULL,
    expected_value VARCHAR(200),
    priority VARCHAR(16) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    message_template VARCHAR(300),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_alert_rules PRIMARY KEY (id),
    CONSTRAINT fk_alert_rules_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE event_records (
    id UUID NOT NULL,
    version BIGINT NOT NULL,
    tenant_id UUID NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    source VARCHAR(120) NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    status VARCHAR(24) NOT NULL,
    processing_attempts INTEGER NOT NULL,
    last_error VARCHAR(400),
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_event_records PRIMARY KEY (id),
    CONSTRAINT fk_event_records_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uk_event_records_tenant_idempotency UNIQUE (tenant_id, idempotency_key)
);

CREATE TABLE notification_records (
    id UUID NOT NULL,
    version BIGINT NOT NULL,
    tenant_id UUID NOT NULL,
    event_record_id UUID NOT NULL,
    alert_rule_id UUID NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(24) NOT NULL,
    error_message VARCHAR(400),
    delivered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_notification_records PRIMARY KEY (id),
    CONSTRAINT fk_notification_records_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_notification_records_event FOREIGN KEY (event_record_id) REFERENCES event_records (id),
    CONSTRAINT fk_notification_records_rule FOREIGN KEY (alert_rule_id) REFERENCES alert_rules (id)
);

CREATE TABLE audit_entries (
    id UUID NOT NULL,
    version BIGINT NOT NULL,
    tenant_id UUID,
    actor_user_id UUID,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_audit_entries PRIMARY KEY (id),
    CONSTRAINT fk_audit_entries_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_users_tenant ON users (tenant_id);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_alert_rules_lookup ON alert_rules (tenant_id, event_type, active);
CREATE INDEX idx_event_records_lookup ON event_records (tenant_id, status, created_at DESC);
CREATE INDEX idx_notification_records_lookup ON notification_records (tenant_id, status, created_at DESC);
CREATE INDEX idx_audit_entries_lookup ON audit_entries (tenant_id, action, created_at DESC);
