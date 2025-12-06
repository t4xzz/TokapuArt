-- Actualizar obras existentes para que tengan contadores en 0 en vez de NULL
-- Ejecutar esto una sola vez en tu base de datos MySQL

UPDATE artworks
SET views_count = COALESCE(views_count, 0),
    favorites_count = COALESCE(favorites_count, 0),
    comments_count = COALESCE(comments_count, 0),
    validation_score = COALESCE(validation_score, 0)
WHERE views_count IS NULL
   OR favorites_count IS NULL
   OR comments_count IS NULL
   OR validation_score IS NULL;
