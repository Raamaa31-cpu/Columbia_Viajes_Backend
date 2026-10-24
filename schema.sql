-- Estructura y datos actuales de columbia_viajes, exportados el 12/10/2026.
-- Incluye las contrasenias tal como estan almacenadas en la base (hashes BCrypt).
-- Al importar, este archivo reemplaza las tablas existentes de la base seleccionada.

-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: columbia_viajes
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ciudades`
--

DROP TABLE IF EXISTS `ciudades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ciudades` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ciudades_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ciudades`
--
-- ORDER BY:  `id`

LOCK TABLES `ciudades` WRITE;
/*!40000 ALTER TABLE `ciudades` DISABLE KEYS */;
INSERT INTO `ciudades` (`id`, `nombre`) VALUES (1,'CABA');
INSERT INTO `ciudades` (`id`, `nombre`) VALUES (2,'Rio de Janeiro');
INSERT INTO `ciudades` (`id`, `nombre`) VALUES (3,'Cusco');
/*!40000 ALTER TABLE `ciudades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hoteles`
--

DROP TABLE IF EXISTS `hoteles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hoteles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(150) NOT NULL,
  `id_ciudad` int unsigned NOT NULL,
  `telefono` varchar(30) NOT NULL,
  `plazas_totales` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_hoteles_ciudad` (`id_ciudad`),
  CONSTRAINT `fk_hoteles_ciudad` FOREIGN KEY (`id_ciudad`) REFERENCES `ciudades` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hoteles`
--
-- ORDER BY:  `id`

LOCK TABLES `hoteles` WRITE;
/*!40000 ALTER TABLE `hoteles` DISABLE KEYS */;
/*!40000 ALTER TABLE `hoteles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paquetes`
--

DROP TABLE IF EXISTS `paquetes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paquetes` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_sucursal` int unsigned NOT NULL,
  `id_vuelo_ida` int unsigned NOT NULL,
  `id_vuelo_vuelta` int unsigned NOT NULL,
  `id_hotel` int unsigned NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text,
  `fecha_llegada_hotel` date NOT NULL,
  `fecha_partida_hotel` date NOT NULL,
  `precio` decimal(12,2) NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `imagen` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_paquetes_sucursal` (`id_sucursal`),
  KEY `fk_paquetes_vuelo_ida` (`id_vuelo_ida`),
  KEY `fk_paquetes_vuelo_vuelta` (`id_vuelo_vuelta`),
  KEY `fk_paquetes_hotel` (`id_hotel`),
  CONSTRAINT `fk_paquetes_hotel` FOREIGN KEY (`id_hotel`) REFERENCES `hoteles` (`id`),
  CONSTRAINT `fk_paquetes_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id`),
  CONSTRAINT `fk_paquetes_vuelo_ida` FOREIGN KEY (`id_vuelo_ida`) REFERENCES `vuelos` (`id`),
  CONSTRAINT `fk_paquetes_vuelo_vuelta` FOREIGN KEY (`id_vuelo_vuelta`) REFERENCES `vuelos` (`id`),
  CONSTRAINT `chk_paquetes_fechas` CHECK ((`fecha_partida_hotel` > `fecha_llegada_hotel`)),
  CONSTRAINT `chk_paquetes_precio` CHECK ((`precio` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paquetes`
--
-- ORDER BY:  `id`

LOCK TABLES `paquetes` WRITE;
/*!40000 ALTER TABLE `paquetes` DISABLE KEYS */;
/*!40000 ALTER TABLE `paquetes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paquetes_reservas`
--

DROP TABLE IF EXISTS `paquetes_reservas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paquetes_reservas` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_paquete` int unsigned NOT NULL,
  `id_turista` int unsigned NOT NULL,
  `clase_vuelo` enum('TURISTA','PRIMERA') NOT NULL,
  `fecha_reserva` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `regimen_hospedaje` enum('MEDIA_PENSION','PENSION_COMPLETA') NOT NULL,
  `precio_reserva` decimal(12,2) NOT NULL,
  `id_vendedor` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_reservas_paquete` (`id_paquete`),
  KEY `fk_reservas_turista` (`id_turista`),
  KEY `fk_reservas_vendedor` (`id_vendedor`),
  CONSTRAINT `fk_reservas_paquete` FOREIGN KEY (`id_paquete`) REFERENCES `paquetes` (`id`),
  CONSTRAINT `fk_reservas_turista` FOREIGN KEY (`id_turista`) REFERENCES `turistas` (`id`),
  CONSTRAINT `fk_reservas_vendedor` FOREIGN KEY (`id_vendedor`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `chk_reservas_precio` CHECK ((`precio_reserva` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paquetes_reservas`
--
-- ORDER BY:  `id`

LOCK TABLES `paquetes_reservas` WRITE;
/*!40000 ALTER TABLE `paquetes_reservas` DISABLE KEYS */;
/*!40000 ALTER TABLE `paquetes_reservas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_roles_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--
-- ORDER BY:  `id`

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` (`id`, `nombre`) VALUES (1,'Cliente');
INSERT INTO `roles` (`id`, `nombre`) VALUES (2,'Vendedor');
INSERT INTO `roles` (`id`, `nombre`) VALUES (3,'Administrador');
INSERT INTO `roles` (`id`, `nombre`) VALUES (4,'Dueño');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sucursales`
--

DROP TABLE IF EXISTS `sucursales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sucursales` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `direccion` varchar(150) NOT NULL,
  `email` varchar(120) NOT NULL,
  `telefono` varchar(30) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sucursales`
--
-- ORDER BY:  `id`

LOCK TABLES `sucursales` WRITE;
/*!40000 ALTER TABLE `sucursales` DISABLE KEYS */;
INSERT INTO `sucursales` (`id`, `direccion`, `email`, `telefono`) VALUES (2,'dire1','suc1@gmail.com','1112345678');
INSERT INTO `sucursales` (`id`, `direccion`, `email`, `telefono`) VALUES (3,'dire2','suc2@gmail.com','1187654321');
/*!40000 ALTER TABLE `sucursales` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turistas`
--

DROP TABLE IF EXISTS `turistas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `turistas` (
  `id` int unsigned NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `direccion` varchar(150) NOT NULL,
  `email` varchar(120) NOT NULL,
  `telefono` varchar(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_turistas_email` (`email`),
  CONSTRAINT `fk_turistas_usuario` FOREIGN KEY (`id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `turistas`
--
-- ORDER BY:  `id`

LOCK TABLES `turistas` WRITE;
/*!40000 ALTER TABLE `turistas` DISABLE KEYS */;
/*!40000 ALTER TABLE `turistas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `contrasenia` varchar(255) NOT NULL,
  `id_rol` int unsigned NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `id_sucursal` int unsigned DEFAULT NULL,
  `version_autenticacion` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_usuarios_nombre` (`nombre`),
  KEY `fk_usuarios_rol` (`id_rol`),
  KEY `fk_usuario_sucursal` (`id_sucursal`),
  CONSTRAINT `fk_usuario_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id`),
  CONSTRAINT `fk_usuarios_rol` FOREIGN KEY (`id_rol`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--
-- ORDER BY:  `id`

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` (`id`, `nombre`, `contrasenia`, `id_rol`, `fecha_creacion`, `id_sucursal`, `version_autenticacion`) VALUES (2,'prueba_admin','$2a$10$mgqaD5KjnNZJfOwUG523GOkpY8GaNsJ1SIqsIdKUOXb7zLArXU2fK',3,'2026-10-09 00:32:48',NULL,3);
INSERT INTO `usuarios` (`id`, `nombre`, `contrasenia`, `id_rol`, `fecha_creacion`, `id_sucursal`, `version_autenticacion`) VALUES (3,'prueba_duenio','$2a$10$Hi7yG0TC2LJlsUMk6uHr.uJHBt/I6E87Ns19FGRkBQsKPlJ3OtgrG',4,'2026-10-09 00:32:48',NULL,0);
INSERT INTO `usuarios` (`id`, `nombre`, `contrasenia`, `id_rol`, `fecha_creacion`, `id_sucursal`, `version_autenticacion`) VALUES (4,'vendedor_a1','$2a$10$BQr88dfKVuuab6X8QF.DaOgjoJL8o4dgJdYLB.7fQlzkSaoWo43GS',2,'2026-10-10 16:57:51',2,0);
INSERT INTO `usuarios` (`id`, `nombre`, `contrasenia`, `id_rol`, `fecha_creacion`, `id_sucursal`, `version_autenticacion`) VALUES (5,'vendedor_a2','$2a$10$zSCxe9FLxQX8IyNlAkPx2OlqwJGhUv6WURi.WmYOfPgCbCYBmtSjK',2,'2026-10-10 16:58:10',2,0);
INSERT INTO `usuarios` (`id`, `nombre`, `contrasenia`, `id_rol`, `fecha_creacion`, `id_sucursal`, `version_autenticacion`) VALUES (6,'vendedor_b1','$2a$10$L7NbMgOc.bEGkDayIRJYruAyQacZ0njw0iaHgivc.CT4xRXDyOmN.',2,'2026-10-10 16:58:20',3,0);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vuelos`
--

DROP TABLE IF EXISTS `vuelos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vuelos` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_ciudad_origen` int unsigned NOT NULL,
  `id_ciudad_destino` int unsigned NOT NULL,
  `fecha_salida` datetime NOT NULL,
  `fecha_llegada` datetime NOT NULL,
  `plazas_turista_totales` int unsigned NOT NULL,
  `plazas_primera_totales` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_vuelos_origen` (`id_ciudad_origen`),
  KEY `fk_vuelos_destino` (`id_ciudad_destino`),
  CONSTRAINT `fk_vuelos_destino` FOREIGN KEY (`id_ciudad_destino`) REFERENCES `ciudades` (`id`),
  CONSTRAINT `fk_vuelos_origen` FOREIGN KEY (`id_ciudad_origen`) REFERENCES `ciudades` (`id`),
  CONSTRAINT `chk_vuelos_ciudades` CHECK ((`id_ciudad_origen` <> `id_ciudad_destino`)),
  CONSTRAINT `chk_vuelos_fechas` CHECK ((`fecha_llegada` > `fecha_salida`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vuelos`
--
-- ORDER BY:  `id`

LOCK TABLES `vuelos` WRITE;
/*!40000 ALTER TABLE `vuelos` DISABLE KEYS */;
/*!40000 ALTER TABLE `vuelos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'columbia_viajes'
--

--
-- Dumping routines for database 'columbia_viajes'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-12  0:57:00
