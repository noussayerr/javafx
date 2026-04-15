-- Initialise la base `pi_projet` pour l'application JavaFX.
-- Compatible MySQL 8.x

CREATE DATABASE IF NOT EXISTS `pi_projet`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `pi_projet`;

-- Table principale des utilisateurs.
-- `password_hash` est prevu pour stocker un hash (jamais un mot de passe en clair).
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(120) NOT NULL,
  `email` VARCHAR(190) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- Colonnes d'extension pour futures fonctionnalites d'authentification.
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `last_login_at` TIMESTAMP NULL DEFAULT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Index utile pour les tris/recherches frequentes.
CREATE INDEX `idx_users_created_at` ON `users` (`created_at`);

-- Table des postes pour le module MVC JavaFX.
CREATE TABLE IF NOT EXISTS `poste` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `titre` VARCHAR(150) NOT NULL,
  `description` TEXT NOT NULL,
  `date_creation` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_poste_date_creation` ON `poste` (`date_creation`);

