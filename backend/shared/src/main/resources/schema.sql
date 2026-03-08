-- DialGenie SQLite Database Schema
-- Production-ready enterprise SaaS platform for AI-powered telecalling

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    phone_number TEXT,
    organization_id TEXT NOT NULL,
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_organization_id ON users(organization_id);

-- Organizations Table
CREATE TABLE IF NOT EXISTS organizations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    subscription_tier TEXT DEFAULT 'STARTER',
    api_calls_limit INTEGER DEFAULT 10000,
    concurrent_calls_limit INTEGER DEFAULT 5,
    storage_limit_gb INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id TEXT PRIMARY KEY,
    organization_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    permissions TEXT, -- JSON array of permissions
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    UNIQUE(organization_id, name)
);

-- User-Role Mappings
CREATE TABLE IF NOT EXISTS user_roles (
    user_id TEXT NOT NULL,
    role_id TEXT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Campaigns Table
CREATE TABLE IF NOT EXISTS campaigns (
    id TEXT PRIMARY KEY,
    organization_id TEXT NOT NULL,
    created_by TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'DRAFT', -- DRAFT, ACTIVE, PAUSED, COMPLETED
    campaign_type TEXT DEFAULT 'OUTBOUND', -- OUTBOUND, INBOUND, SURVEY
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    total_leads INTEGER DEFAULT 0,
    processed_leads INTEGER DEFAULT 0,
    successful_calls INTEGER DEFAULT 0,
    failed_calls INTEGER DEFAULT 0,
    avg_call_duration_seconds INTEGER DEFAULT 0,
    ai_model_version TEXT DEFAULT 'v1.0',
    voice_config TEXT, -- JSON: voice_id, speed, accent
    greeting_message TEXT,
    closing_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_campaigns_organization_id ON campaigns(organization_id);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_created_by ON campaigns(created_by);

-- Leads Table
CREATE TABLE IF NOT EXISTS leads (
    id TEXT PRIMARY KEY,
    campaign_id TEXT NOT NULL,
    organization_id TEXT NOT NULL,
    name TEXT NOT NULL,
    phone_number TEXT NOT NULL,
    email TEXT,
    concern TEXT,
    solution TEXT,
    lead_score INTEGER DEFAULT 0,
    status TEXT DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, FAILED, DUPLICATE
    dnd_flag BOOLEAN DEFAULT 0,
    last_called_at TIMESTAMP,
    next_call_scheduled_at TIMESTAMP,
    total_attempts INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_leads_campaign_id ON leads(campaign_id);
CREATE INDEX idx_leads_phone_number ON leads(phone_number);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_organization_id ON leads(organization_id);
CREATE INDEX idx_leads_email ON leads(email);

-- Call Logs Table
CREATE TABLE IF NOT EXISTS call_logs (
    id TEXT PRIMARY KEY,
    lead_id TEXT NOT NULL,
    campaign_id TEXT NOT NULL,
    organization_id TEXT NOT NULL,
    twilio_call_sid TEXT UNIQUE,
    phone_number TEXT NOT NULL,
    call_status TEXT, -- INITIATED, RINGING, IN_PROGRESS, COMPLETED, FAILED
    outcome TEXT, -- INTERESTED, NOT_INTERESTED, CALL_BACK, NO_RESPONSE
    call_duration_seconds INTEGER DEFAULT 0,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    recording_url TEXT,
    transcript_id TEXT,
    sentiment_score FLOAT, -- -1.0 to 1.0
    sentiment_label TEXT, -- POSITIVE, NEUTRAL, NEGATIVE
    ai_confidence_score FLOAT,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lead_id) REFERENCES leads(id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_call_logs_lead_id ON call_logs(lead_id);
CREATE INDEX idx_call_logs_campaign_id ON call_logs(campaign_id);
CREATE INDEX idx_call_logs_status ON call_logs(call_status);
CREATE INDEX idx_call_logs_twilio_call_sid ON call_logs(twilio_call_sid);
CREATE INDEX idx_call_logs_created_at ON call_logs(created_at);

-- Transcripts Table
CREATE TABLE IF NOT EXISTS transcripts (
    id TEXT PRIMARY KEY,
    call_log_id TEXT NOT NULL,
    lead_id TEXT NOT NULL,
    organization_id TEXT NOT NULL,
    full_transcript TEXT, -- Complete conversation
    conversation_segments TEXT, -- JSON array of turns
    ai_response_count INTEGER DEFAULT 0,
    user_response_count INTEGER DEFAULT 0,
    objections_raised TEXT, -- JSON array
    objections_handled INT DEFAULT 0,
    intent_detected TEXT,
    key_points TEXT, -- JSON array
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (call_log_id) REFERENCES call_logs(id),
    FOREIGN KEY (lead_id) REFERENCES leads(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_transcripts_call_log_id ON transcripts(call_log_id);
CREATE INDEX idx_transcripts_lead_id ON transcripts(lead_id);

-- Call Summary Table
CREATE TABLE IF NOT EXISTS call_summaries (
    id TEXT PRIMARY KEY,
    call_log_id TEXT NOT NULL,
    lead_id TEXT NOT NULL,
    organization_id TEXT NOT NULL,
    summary_text TEXT NOT NULL,
    action_items TEXT, -- JSON array
    recommended_follow_up_date TIMESTAMP,
    follow_up_priority TEXT, -- HIGH, MEDIUM, LOW
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (call_log_id) REFERENCES call_logs(id),
    FOREIGN KEY (lead_id) REFERENCES leads(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_call_summaries_call_log_id ON call_summaries(call_log_id);
CREATE INDEX idx_call_summaries_lead_id ON call_summaries(lead_id);

-- Follow-ups Table
CREATE TABLE IF NOT EXISTS follow_ups (
    id TEXT PRIMARY KEY,
    lead_id TEXT NOT NULL,
    campaign_id TEXT NOT NULL,
    organization_id TEXT NOT NULL,
    original_call_id TEXT,
    scheduled_for TIMESTAMP NOT NULL,
    follow_up_type TEXT, -- CALL, EMAIL, SMS
    reason TEXT,
    status TEXT DEFAULT 'PENDING', -- PENDING, SCHEDULED, COMPLETED, CANCELLED
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lead_id) REFERENCES leads(id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (original_call_id) REFERENCES call_logs(id)
);

CREATE INDEX idx_follow_ups_lead_id ON follow_ups(lead_id);
CREATE INDEX idx_follow_ups_campaign_id ON follow_ups(campaign_id);
CREATE INDEX idx_follow_ups_status ON follow_ups(status);
CREATE INDEX idx_follow_ups_scheduled_for ON follow_ups(scheduled_for);

-- Lead Scores Table
CREATE TABLE IF NOT EXISTS lead_scores (
    id TEXT PRIMARY KEY,
    lead_id TEXT NOT NULL,
    organization_id TEXT NOT NULL,
    score INTEGER NOT NULL,
    score_breakdown TEXT, -- JSON: engagement=30, response_quality=40, intent=30
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    calculated_by TEXT, -- RULE_ENGINE, AI_MODEL
    FOREIGN KEY (lead_id) REFERENCES leads(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    UNIQUE(lead_id)
);

CREATE INDEX idx_lead_scores_lead_id ON lead_scores(lead_id);

-- Audit Log Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id TEXT PRIMARY KEY,
    organization_id TEXT NOT NULL,
    user_id TEXT,
    action TEXT NOT NULL,
    resource_type TEXT, -- USER, CAMPAIGN, LEAD, CALL
    resource_id TEXT,
    old_values TEXT, -- JSON
    new_values TEXT, -- JSON
    ip_address TEXT,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_audit_logs_organization_id ON audit_logs(organization_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_resource_id ON audit_logs(resource_id);

-- Campaign Metrics Table
CREATE TABLE IF NOT EXISTS campaign_metrics (
    id TEXT PRIMARY KEY,
    campaign_id TEXT NOT NULL,
    organization_id TEXT NOT NULL,
    date DATE NOT NULL,
    total_calls INTEGER DEFAULT 0,
    successful_calls INTEGER DEFAULT 0,
    failed_calls INTEGER DEFAULT 0,
    avg_duration_seconds FLOAT DEFAULT 0,
    interested_count INTEGER DEFAULT 0,
    not_interested_count INTEGER DEFAULT 0,
    callback_count INTEGER DEFAULT 0,
    no_response_count INTEGER DEFAULT 0,
    success_rate FLOAT DEFAULT 0,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    UNIQUE(campaign_id, date)
);

CREATE INDEX idx_campaign_metrics_campaign_id ON campaign_metrics(campaign_id);
CREATE INDEX idx_campaign_metrics_date ON campaign_metrics(date);

-- API Usage Log Table
CREATE TABLE IF NOT EXISTS api_usage_logs (
    id TEXT PRIMARY KEY,
    organization_id TEXT NOT NULL,
    endpoint TEXT NOT NULL,
    method TEXT,
    status_code INTEGER,
    response_time_ms INTEGER,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_api_usage_logs_organization_id ON api_usage_logs(organization_id);
CREATE INDEX idx_api_usage_logs_created_at ON api_usage_logs(created_at);

-- Twilio Webhook Events Table
CREATE TABLE IF NOT EXISTS twilio_webhook_events (
    id TEXT PRIMARY KEY,
    call_log_id TEXT,
    organization_id TEXT NOT NULL,
    event_type TEXT,
    twilio_call_sid TEXT,
    payload TEXT, -- Full JSON payload
    status TEXT DEFAULT 'PROCESSED', -- RECEIVED, PROCESSED, FAILED
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    FOREIGN KEY (call_log_id) REFERENCES call_logs(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_twilio_webhook_events_call_log_id ON twilio_webhook_events(call_log_id);
CREATE INDEX idx_twilio_webhook_events_status ON twilio_webhook_events(status);
CREATE INDEX idx_twilio_webhook_events_created_at ON twilio_webhook_events(created_at);

-- ============================================
-- TEST DATA & DEFAULT USERS
-- ============================================

-- Insert default organization
INSERT OR IGNORE INTO organizations (id, name, subscription_tier, api_calls_limit, concurrent_calls_limit, storage_limit_gb, is_active, created_at, updated_at)
VALUES ('org-001', 'DialGenie Demo', 'ENTERPRISE', 100000, 100, 500, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT OR IGNORE INTO organizations (id, name, subscription_tier, api_calls_limit, concurrent_calls_limit, storage_limit_gb, is_active, created_at, updated_at)
VALUES ('org-002', 'Test Organization', 'STARTER', 10000, 5, 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert default roles
INSERT OR IGNORE INTO roles (id, organization_id, name, description, permissions, is_active, created_at)
VALUES ('role-admin-001', 'org-001', 'ADMIN', 'Administrator with full access', '["READ", "WRITE", "DELETE", "MANAGE_USERS"]', 1, CURRENT_TIMESTAMP);

INSERT OR IGNORE INTO roles (id, organization_id, name, description, permissions, is_active, created_at)
VALUES ('role-user-001', 'org-001', 'USER', 'Regular user with read/write access', '["READ", "WRITE"]', 1, CURRENT_TIMESTAMP);

INSERT OR IGNORE INTO roles (id, organization_id, name, description, permissions, is_active, created_at)
VALUES ('role-viewer-001', 'org-001', 'VIEWER', 'Read-only access', '["READ"]', 1, CURRENT_TIMESTAMP);

-- Insert default admin user
-- Password: admin@123 (SHA-256: should be hashed in production, using plain for demo)
-- Actual hash: $2a$10$ZIzJ1yCwXjPFhKGzZI5Cp.FaVnS3OJmTzKj6pK3Jp1mV7K2Q2A2Di
INSERT OR IGNORE INTO users (id, email, password_hash, first_name, last_name, phone_number, organization_id, is_active, created_at, updated_at)
VALUES ('usr-admin-001', 'admin@dialgenie.com', '$2a$10$ZIzJ1yCwXjPFhKGzZI5Cp.FaVnS3OJmTzKj6pK3Jp1mV7K2Q2A2Di', 'Admin', 'User', '+1234567890', 'org-001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test user
-- Password: test@123
INSERT OR IGNORE INTO users (id, email, password_hash, first_name, last_name, phone_number, organization_id, is_active, created_at, updated_at)
VALUES ('usr-test-001', 'test@dialgenie.com', '$2a$10$ZIzJ1yCwXjPFhKGzZI5Cp.FaVnS3OJmTzKj6pK3Jp1mV7K2Q2A2Di', 'Test', 'User', '+1234567891', 'org-001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign admin role to admin user
INSERT OR IGNORE INTO user_roles (user_id, role_id, assigned_at)
VALUES ('usr-admin-001', 'role-admin-001', CURRENT_TIMESTAMP);

-- Assign user role to test user
INSERT OR IGNORE INTO user_roles (user_id, role_id, assigned_at)
VALUES ('usr-test-001', 'role-user-001', CURRENT_TIMESTAMP);
