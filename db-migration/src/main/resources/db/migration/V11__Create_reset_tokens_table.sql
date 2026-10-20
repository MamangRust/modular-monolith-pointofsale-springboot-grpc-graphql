-- Create reset_tokens table
-- Backs the forgot-password flow: one short-lived (15 min) token per user.
CREATE TABLE IF NOT EXISTS reset_tokens (
    reset_token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token VARCHAR(1100) NOT NULL UNIQUE,
    expiration TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_reset_tokens_user_id ON reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_reset_tokens_token ON reset_tokens (token);
CREATE INDEX IF NOT EXISTS idx_reset_tokens_expiration ON reset_tokens (expiration);
