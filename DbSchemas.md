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
    model VARCHAR(255),
    custom_prompt_id UUID,
    fav BOOLEAN NOT NULL,
    FOREIGN KEY (custom_prompt_id) REFERENCES custom_prompt(id)
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

CREATE TABLE custom_prompt (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE prompt_messages (
    id UUID PRIMARY KEY,
    role VARCHAR(255),
    content TEXT,
    custom_prompt_id UUID,
    FOREIGN KEY (custom_prompt_id) REFERENCES custom_prompt(id)
);

CREATE TABLE prompt_params (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    value VARCHAR(255),
    custom_prompt_id UUID,
    FOREIGN KEY (custom_prompt_id) REFERENCES custom_prompt(id)
);

-- Optional: Add indexes
CREATE INDEX idx_app_message_chat ON app_message(chat_id);
CREATE INDEX idx_chat_custom_prompt ON chat(custom_prompt_id);
CREATE INDEX idx_prompt_messages_prompt ON prompt_messages(custom_prompt_id);
CREATE INDEX idx_prompt_params_prompt ON prompt_params(custom_prompt_id);

```