CREATE TABLE IF NOT EXISTS devices (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    brand         VARCHAR(255) NOT NULL,
    state         VARCHAR(20)  NOT NULL CHECK (state IN ('AVAILABLE', 'IN_USE', 'INACTIVE')),
    creation_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_devices_brand ON devices (brand);
CREATE INDEX idx_devices_state ON devices (state);
