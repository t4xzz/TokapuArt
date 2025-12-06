-- Tabla para reacciones/likes a obras
CREATE TABLE IF NOT EXISTS artwork_reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artwork_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction_type ENUM('LIKE', 'LOVE', 'WOW', 'CLAP') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_artwork_reaction (user_id, artwork_id)
);

-- Agregar columnas de contadores de reacciones a artworks
ALTER TABLE artworks
ADD COLUMN likes_count INT DEFAULT 0,
ADD COLUMN loves_count INT DEFAULT 0,
ADD COLUMN wows_count INT DEFAULT 0,
ADD COLUMN claps_count INT DEFAULT 0;
