CREATE TABLE IF NOT EXISTS batch_job (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    result TEXT,
    error_message TEXT,
    records_processed INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_job_name ON batch_job(job_name);
CREATE INDEX idx_status ON batch_job(status);
CREATE INDEX idx_created_at ON batch_job(created_at);
