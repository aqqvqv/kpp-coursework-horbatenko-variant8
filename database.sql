DROP DATABASE IF EXISTS water_quality;
CREATE DATABASE water_quality CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE water_quality;

CREATE TABLE observation_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(160) NOT NULL,
    water_body VARCHAR(160) NOT NULL,
    sensitivity_score DOUBLE NOT NULL
);

CREATE TABLE sample (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    point_id BIGINT NOT NULL,
    sample_date VARCHAR(20) NOT NULL,
    state VARCHAR(30) NOT NULL,
    f1 DOUBLE NOT NULL,
    f2 DOUBLE NOT NULL,
    f3 DOUBLE NOT NULL,
    integral_score DOUBLE NOT NULL,
    FOREIGN KEY (point_id) REFERENCES observation_point(id) ON DELETE CASCADE
);

CREATE TABLE measurement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sample_id BIGINT NOT NULL,
    parameter_name VARCHAR(80) NOT NULL,
    measured_value DOUBLE NOT NULL,
    norm_value DOUBLE NOT NULL,
    unit VARCHAR(30) NOT NULL,
    FOREIGN KEY (sample_id) REFERENCES sample(id) ON DELETE CASCADE
);

CREATE TABLE inspection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sample_id BIGINT NOT NULL,
    inspector_name VARCHAR(160) NOT NULL,
    inspection_date VARCHAR(20) NOT NULL,
    conclusion VARCHAR(500) NOT NULL,
    FOREIGN KEY (sample_id) REFERENCES sample(id) ON DELETE CASCADE
);

INSERT INTO observation_point (code, name, water_body, sensitivity_score) VALUES
('WP-01', 'Водозабір центральний', 'р. Ворскла', 80.0),
('WP-02', 'Південний створ', 'р. Ворскла', 50.0);

INSERT INTO sample (point_id, sample_date, state, f1, f2, f3, integral_score) VALUES
(1, '2026-10-15', 'REGISTERED', 40.0, 20.0, 80.0, 43.00),
(2, '2026-10-18', 'VALIDATED', 0.0, 0.0, 50.0, 12.50);

INSERT INTO measurement (sample_id, parameter_name, measured_value, norm_value, unit) VALUES
(1, 'Нітрати (NO3-)', 63.0, 45.0, 'мг/дм3'),
(1, 'Розчинений кисень', 6.5, 6.0, 'мг/дм3'),
(2, 'Залізо загальне', 0.25, 0.30, 'мг/дм3');

INSERT INTO inspection (sample_id, inspector_name, inspection_date, conclusion) VALUES
(1, 'Коваленко О. І.', '2026-10-16', 'Перевищення норми нітратів на 40%, рекомендовано повторний відбір'),
(2, 'Бондаренко М. В.', '2026-10-19', 'Показники в межах ГДК, пробу валідовано');
