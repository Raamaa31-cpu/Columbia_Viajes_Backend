-- ============================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS Y TABLAS (Columbia Viajes)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `paquetes_reservas`;
DROP TABLE IF EXISTS `paquetes`;
DROP TABLE IF EXISTS `vuelos`;
DROP TABLE IF EXISTS `hoteles`;
DROP TABLE IF EXISTS `turistas`;
DROP TABLE IF EXISTS `usuarios`;
DROP TABLE IF EXISTS `sucursales`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `ciudades`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Catálogos base (Sin dependencias)
CREATE TABLE `ciudades` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ciudades_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `roles` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_roles_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `sucursales` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `direccion` VARCHAR(150) NOT NULL,
  `email` VARCHAR(120) NOT NULL,
  `telefono` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Entidades dependientes de catálogos base
CREATE TABLE `usuarios` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `contrasenia` VARCHAR(255) NOT NULL,
  `id_rol` INT UNSIGNED NOT NULL,
  `id_sucursal` INT UNSIGNED DEFAULT NULL,
  `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_usuarios_nombre` (`nombre`),
  KEY `fk_usuarios_rol` (`id_rol`),
  KEY `fk_usuario_sucursal` (`id_sucursal`),
  CONSTRAINT `fk_usuarios_rol` FOREIGN KEY (`id_rol`) REFERENCES `roles` (`id`),
  CONSTRAINT `fk_usuario_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Relación 1 a 1 con usuarios (comparte el mismo ID)
CREATE TABLE `turistas` (
  `id` INT UNSIGNED NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `apellidos` VARCHAR(100) NOT NULL,
  `direccion` VARCHAR(150) NOT NULL,
  `email` VARCHAR(120) NOT NULL,
  `telefono` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_turistas_usuario` FOREIGN KEY (`id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `hoteles` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `direccion` VARCHAR(150) NOT NULL,
  `id_ciudad` INT UNSIGNED NOT NULL,
  `telefono` VARCHAR(30) NOT NULL,
  `plazas_totales` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_hoteles_ciudad` (`id_ciudad`),
  CONSTRAINT `fk_hoteles_ciudad` FOREIGN KEY (`id_ciudad`) REFERENCES `ciudades` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `vuelos` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_ciudad_origen` INT UNSIGNED NOT NULL,
  `id_ciudad_destino` INT UNSIGNED NOT NULL,
  `fecha_salida` DATETIME NOT NULL,
  `fecha_llegada` DATETIME NOT NULL,
  `plazas_turista_totales` INT UNSIGNED NOT NULL,
  `plazas_primera_totales` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_vuelos_origen` (`id_ciudad_origen`),
  KEY `fk_vuelos_destino` (`id_ciudad_destino`),
  CONSTRAINT `fk_vuelos_origen` FOREIGN KEY (`id_ciudad_origen`) REFERENCES `ciudades` (`id`),
  CONSTRAINT `fk_vuelos_destino` FOREIGN KEY (`id_ciudad_destino`) REFERENCES `ciudades` (`id`),
  CONSTRAINT `chk_vuelos_ciudades` CHECK ((`id_ciudad_origen` <> `id_ciudad_destino`)),
  CONSTRAINT `chk_vuelos_fechas` CHECK ((`fecha_llegada` > `fecha_salida`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Entidades compuestas y operacionales
CREATE TABLE `paquetes` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_sucursal` INT UNSIGNED NOT NULL,
  `id_vuelo_ida` INT UNSIGNED NOT NULL,
  `id_vuelo_vuelta` INT UNSIGNED NOT NULL,
  `id_hotel` INT UNSIGNED NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `descripcion` TEXT,
  `fecha_llegada_hotel` DATE NOT NULL,
  `fecha_partida_hotel` DATE NOT NULL,
  `precio` DECIMAL(12,2) NOT NULL,
  `imagen` VARCHAR(255) DEFAULT NULL,
  `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_paquetes_sucursal` (`id_sucursal`),
  KEY `fk_paquetes_vuelo_ida` (`id_vuelo_ida`),
  KEY `fk_paquetes_vuelo_vuelta` (`id_vuelo_vuelta`),
  KEY `fk_paquetes_hotel` (`id_hotel`),
  CONSTRAINT `fk_paquetes_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id`),
  CONSTRAINT `fk_paquetes_vuelo_ida` FOREIGN KEY (`id_vuelo_ida`) REFERENCES `vuelos` (`id`),
  CONSTRAINT `fk_paquetes_vuelo_vuelta` FOREIGN KEY (`id_vuelo_vuelta`) REFERENCES `vuelos` (`id`),
  CONSTRAINT `fk_paquetes_hotel` FOREIGN KEY (`id_hotel`) REFERENCES `hoteles` (`id`),
  CONSTRAINT `chk_paquetes_fechas` CHECK ((`fecha_partida_hotel` > `fecha_llegada_hotel`)),
  CONSTRAINT `chk_paquetes_precio` CHECK ((`precio` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `paquetes_reservas` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_paquete` INT UNSIGNED NOT NULL,
  `id_turista` INT UNSIGNED NOT NULL,
  `id_vendedor` INT UNSIGNED NOT NULL,
  `clase_vuelo` ENUM('TURISTA','PRIMERA') NOT NULL,
  `regimen_hospedaje` ENUM('MEDIA_PENSION','PENSION_COMPLETA') NOT NULL,
  `precio_reserva` DECIMAL(12,2) NOT NULL,
  `fecha_reserva` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_reservas_paquete` (`id_paquete`),
  KEY `fk_reservas_turista` (`id_turista`),
  KEY `fk_reservas_vendedor` (`id_vendedor`),
  CONSTRAINT `fk_reservas_paquete` FOREIGN KEY (`id_paquete`) REFERENCES `paquetes` (`id`),
  CONSTRAINT `fk_reservas_turista` FOREIGN KEY (`id_turista`) REFERENCES `turistas` (`id`),
  CONSTRAINT `fk_reservas_vendedor` FOREIGN KEY (`id_vendedor`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `chk_reservas_precio` CHECK ((`precio_reserva` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- DATOS INICIALES (Semilla)
-- ============================================================

-- Roles iniciales requeridos por Spring Security / App
INSERT INTO `roles` (`id`, `nombre`) VALUES
(1, 'Administrador'),
(2, 'Vendedor'),
(3, 'Cliente'),
(4, 'Dueño');

-- Ciudades iniciales
INSERT INTO `ciudades` (`nombre`) VALUES
('CABA'),
('Rio de Janeiro'),
('Cusco');
