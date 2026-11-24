-- Crea el usuario y la base "promou" que usa el backend en local (ver application.yml).
-- Se puede ejecutar varias veces: si ya existen, no los duplica.
-- Las tablas NO se crean aqui: Flyway aplica V1__esquema.sql y V2__categorias.sql al arrancar el backend.

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'promou') THEN
        CREATE ROLE promou LOGIN PASSWORD 'promou';
    ELSE
        ALTER ROLE promou WITH LOGIN PASSWORD 'promou';
    END IF;
END
$$;

SELECT 'CREATE DATABASE promou OWNER promou ENCODING ''UTF8'' TEMPLATE template0'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'promou')\gexec

GRANT ALL PRIVILEGES ON DATABASE promou TO promou;
