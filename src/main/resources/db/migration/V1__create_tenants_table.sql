CREATE TABLE tenants (
    id UUID NOT NULL,
    version BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uk_tenants_slug UNIQUE (slug)
);

CREATE INDEX idx_tenants_status ON tenants (status);
CREATE INDEX idx_tenants_created_at ON tenants (created_at DESC);
