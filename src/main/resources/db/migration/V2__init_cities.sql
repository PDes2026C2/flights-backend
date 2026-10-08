CREATE TABLE IF NOT EXISTS world.cities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country_iso VARCHAR(10) NOT NULL,
    CONSTRAINT fk_city_country FOREIGN KEY (country_iso) REFERENCES world.countries(iso_code),
    CONSTRAINT uk_city_name_country UNIQUE (name, country_iso)
);

INSERT INTO world.cities (id, name, country_iso) VALUES
(1, 'Buenos Aires', 'AR'),
(2, 'Córdoba', 'AR'),
(3, 'Bariloche', 'AR'),
(4, 'Mendoza', 'AR'),
(5, 'Rosario', 'AR'),
(6, 'Salta', 'AR'),
(11, 'São Paulo', 'BR'),
(12, 'Río de Janeiro', 'BR'),
(13, 'Florianópolis', 'BR'),
(21, 'Nueva York', 'US'),
(22, 'Los Ángeles', 'US'),
(23, 'Chicago', 'US'),
(24, 'Miami', 'US'),
(31, 'Madrid', 'ES'),
(32, 'Barcelona', 'ES'),
(51, 'Roma', 'IT'),
(61, 'París', 'FR'),
(71, 'Berlín', 'DE'),
(81, 'Londres', 'GB'),
(91, 'Tokio', 'JP')
ON CONFLICT (id) DO NOTHING;

SELECT setval('world.cities_id_seq', (SELECT COALESCE(MAX(id), 1) FROM world.cities));
