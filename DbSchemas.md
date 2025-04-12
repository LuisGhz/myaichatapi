# Database Schemas

## Creating the Database

```sql
-- Create database
CREATE DATABASE myaichat;

-- Connect to the database
\c myaichat
```

## Table Definitions

```sql
-- Create tables
CREATE TABLE chat (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    created_at TIMESTAMP,
    model VARCHAR(255)
);

CREATE TABLE app_message (
    id UUID PRIMARY KEY,
    role VARCHAR(255),
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    total_tokens INTEGER,
    image_url TEXT,
    chat_id UUID,
    FOREIGN KEY (chat_id) REFERENCES chat(id)
);

-- Optional: Add indexes
CREATE INDEX idx_app_message_chat ON app_message(chat_id);