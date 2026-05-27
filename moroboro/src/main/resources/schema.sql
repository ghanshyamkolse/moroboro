CREATE TABLE IF NOT EXISTS app_user (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    youtube_handle VARCHAR(255) NOT NULL,
    channel_id VARCHAR(255),
    channel_title VARCHAR(255),
    channel_avatar VARCHAR(511)
);
