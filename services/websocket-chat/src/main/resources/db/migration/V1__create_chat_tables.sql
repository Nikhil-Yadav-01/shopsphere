CREATE TABLE IF NOT EXISTS conversation
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    user1_id
    BIGINT
    NOT
    NULL,
    user2_id
    BIGINT
    NOT
    NULL,
    created_at
    TIMESTAMP
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP,
    last_message_at
    TIMESTAMP
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_message
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    conversation_id
    BIGINT
    NOT
    NULL
    REFERENCES
    conversation
(
    id
) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_conversation_id ON conversation (user1_id, user2_id);
CREATE INDEX idx_chat_conversation ON chat_message (conversation_id);
CREATE INDEX idx_chat_sender ON chat_message (sender_id);
CREATE INDEX idx_chat_created ON chat_message (created_at);
