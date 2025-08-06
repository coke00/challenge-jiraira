-- Script para inicializar la base de datos tenpo_db incorporado en docker_compose
-- Crear la base de datos si no existe
SELECT 'CREATE DATABASE tenpo_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'tenpo_db')\gexec

-- Conectar a la base de datos
\c tenpo_db;

-- Crear extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- creamos una funcion para actualizar la columna updated_at al momento de actualizar
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Mensaje de confirmación
SELECT 'Database initialization completed successfully' as status;
