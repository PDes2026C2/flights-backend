CREATE SCHEMA IF NOT EXISTS world;

CREATE TABLE IF NOT EXISTS world.countries (
    iso_code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO world.countries (iso_code, name) VALUES
('AR', 'Argentina'),
('BR', 'Brasil'),
('US', 'Estados Unidos'),
('ES', 'España'),
('IT', 'Italia'),
('FR', 'Francia'),
('DE', 'Alemania'),
('GB', 'Reino Unido'),
('JP', 'Japón')
ON CONFLICT (iso_code) DO NOTHING;
