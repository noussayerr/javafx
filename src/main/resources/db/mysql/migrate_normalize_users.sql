-- Migration unique: normalisation definitive de la table utilisateur.
-- Compatible MySQL 8.x
-- Usage: executer ce script une seule fois sur la base cible.

CREATE DATABASE IF NOT EXISTS `pi_projet`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `pi_projet`;

DROP PROCEDURE IF EXISTS `sp_normalize_users`;
DELIMITER $$

CREATE PROCEDURE `sp_normalize_users`()
BEGIN
    DECLARE has_users INT DEFAULT 0;
    DECLARE has_legacy_user INT DEFAULT 0;

    DECLARE users_has_password INT DEFAULT 0;
    DECLARE users_has_username INT DEFAULT 0;
    DECLARE users_has_full_name INT DEFAULT 0;

    DECLARE legacy_has_id INT DEFAULT 0;
    DECLARE legacy_has_email INT DEFAULT 0;
    DECLARE legacy_has_name INT DEFAULT 0;
    DECLARE legacy_has_username INT DEFAULT 0;
    DECLARE legacy_has_full_name INT DEFAULT 0;
    DECLARE legacy_has_password INT DEFAULT 0;
    DECLARE legacy_has_password_hash INT DEFAULT 0;
    DECLARE legacy_has_roles INT DEFAULT 0;
    DECLARE legacy_has_role_type INT DEFAULT 0;
    DECLARE legacy_has_created_at INT DEFAULT 0;
    DECLARE legacy_has_updated_at INT DEFAULT 0;
    DECLARE legacy_has_is_verified INT DEFAULT 0;
    DECLARE legacy_has_is_active INT DEFAULT 0;
    DECLARE legacy_has_verification_code INT DEFAULT 0;
    DECLARE legacy_has_verification_code_expires_at INT DEFAULT 0;
    DECLARE legacy_has_password_reset_code INT DEFAULT 0;
    DECLARE legacy_has_password_reset_code_expires_at INT DEFAULT 0;
    DECLARE legacy_has_water_intake INT DEFAULT 0;

    DECLARE idx_email_exists INT DEFAULT 0;
    DECLARE duplicate_email_count INT DEFAULT 0;

    -- 1) Table canonique `users`
    SELECT COUNT(*) INTO has_users
    FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'users';

    IF has_users = 0 THEN
        CREATE TABLE `users` (
            `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
            `email` VARCHAR(190) NULL,
            `name` VARCHAR(120) NULL,
            `password_hash` VARCHAR(255) NULL,
            `roles` JSON NULL,
            `role_type` VARCHAR(50) NULL,
            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            `is_verified` TINYINT(1) NOT NULL DEFAULT 0,
            `verification_code` VARCHAR(120) NULL,
            `verification_code_expires_at` TIMESTAMP NULL DEFAULT NULL,
            `password_reset_code` VARCHAR(120) NULL,
            `password_reset_code_expires_at` TIMESTAMP NULL DEFAULT NULL,
            `water_intake` JSON NULL,
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB
          DEFAULT CHARSET=utf8mb4
          COLLATE=utf8mb4_unicode_ci;
    END IF;

    -- 2) Ajouter les colonnes manquantes sur `users` (idempotent)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'email'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `email` VARCHAR(190) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'name'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `name` VARCHAR(120) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'password_hash'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `password_hash` VARCHAR(255) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'roles'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `roles` JSON NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'role_type'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `role_type` VARCHAR(50) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'is_verified'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `is_verified` TINYINT(1) NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'verification_code'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `verification_code` VARCHAR(120) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'verification_code_expires_at'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `verification_code_expires_at` TIMESTAMP NULL DEFAULT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'password_reset_code'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `password_reset_code` VARCHAR(120) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'password_reset_code_expires_at'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `password_reset_code_expires_at` TIMESTAMP NULL DEFAULT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'water_intake'
    ) THEN
        ALTER TABLE `users` ADD COLUMN `water_intake` JSON NULL;
    END IF;

    -- 3) Backfill depuis colonnes alternatives deja presentes dans `users`
    SELECT COUNT(*) INTO users_has_password
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'password';

    SELECT COUNT(*) INTO users_has_username
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'username';

    SELECT COUNT(*) INTO users_has_full_name
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'full_name';

    IF users_has_password = 1 THEN
        UPDATE `users`
        SET `password_hash` = `password`
        WHERE (`password_hash` IS NULL OR `password_hash` = '')
          AND `password` IS NOT NULL AND `password` <> '';
    END IF;

    IF users_has_username = 1 THEN
        UPDATE `users`
        SET `name` = `username`
        WHERE (`name` IS NULL OR `name` = '')
          AND `username` IS NOT NULL AND `username` <> '';
    END IF;

    IF users_has_full_name = 1 THEN
        UPDATE `users`
        SET `name` = `full_name`
        WHERE (`name` IS NULL OR `name` = '')
          AND `full_name` IS NOT NULL AND `full_name` <> '';
    END IF;

    -- Valeurs par defaut metier pour eviter les champs critiques vides.
    UPDATE `users`
    SET `roles` = JSON_ARRAY('ROLE_USER')
    WHERE `roles` IS NULL;

    UPDATE `users`
    SET `role_type` = 'user'
    WHERE `role_type` IS NULL OR TRIM(`role_type`) = '';

    UPDATE `users`
    SET `name` = CONCAT('user_', `id`)
    WHERE `name` IS NULL OR TRIM(`name`) = '';

    -- Email vide -> valeur technique unique, pour pouvoir poser un index unique proprement.
    UPDATE `users`
    SET `email` = CONCAT('unknown+', `id`, '@local.invalid')
    WHERE `email` IS NULL OR TRIM(`email`) = '';

    -- 4) Migration depuis la table legacy `user` vers `users`
    SELECT COUNT(*) INTO has_legacy_user
    FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'user';

    IF has_legacy_user = 1 THEN
        SELECT COUNT(*) INTO legacy_has_id FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'id';
        SELECT COUNT(*) INTO legacy_has_email FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'email';
        SELECT COUNT(*) INTO legacy_has_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'name';
        SELECT COUNT(*) INTO legacy_has_username FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'username';
        SELECT COUNT(*) INTO legacy_has_full_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'full_name';
        SELECT COUNT(*) INTO legacy_has_password FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'password';
        SELECT COUNT(*) INTO legacy_has_password_hash FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'password_hash';
        SELECT COUNT(*) INTO legacy_has_roles FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'roles';
        SELECT COUNT(*) INTO legacy_has_role_type FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'role_type';
        SELECT COUNT(*) INTO legacy_has_created_at FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'created_at';
        SELECT COUNT(*) INTO legacy_has_updated_at FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'updated_at';
        SELECT COUNT(*) INTO legacy_has_is_verified FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'is_verified';
        SELECT COUNT(*) INTO legacy_has_is_active FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'is_active';
        SELECT COUNT(*) INTO legacy_has_verification_code FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'verification_code';
        SELECT COUNT(*) INTO legacy_has_verification_code_expires_at FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'verification_code_expires_at';
        SELECT COUNT(*) INTO legacy_has_password_reset_code FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'password_reset_code';
        SELECT COUNT(*) INTO legacy_has_password_reset_code_expires_at FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'password_reset_code_expires_at';
        SELECT COUNT(*) INTO legacy_has_water_intake FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'water_intake';

        SET @legacy_id_expr = IF(legacy_has_id = 1, 'u.`id`', 'NULL');
        SET @legacy_email_expr = IF(legacy_has_email = 1,
            'NULLIF(TRIM(u.`email`), '''')',
            'NULL');

        IF legacy_has_name = 1 THEN
            SET @legacy_name_expr = 'NULLIF(TRIM(u.`name`), '''')';
        ELSEIF legacy_has_username = 1 THEN
            SET @legacy_name_expr = 'NULLIF(TRIM(u.`username`), '''')';
        ELSEIF legacy_has_full_name = 1 THEN
            SET @legacy_name_expr = 'NULLIF(TRIM(u.`full_name`), '''')';
        ELSE
            SET @legacy_name_expr = 'NULL';
        END IF;

        IF legacy_has_password_hash = 1 THEN
            SET @legacy_password_expr = 'NULLIF(u.`password_hash`, '''')';
        ELSEIF legacy_has_password = 1 THEN
            SET @legacy_password_expr = 'NULLIF(u.`password`, '''')';
        ELSE
            SET @legacy_password_expr = 'NULL';
        END IF;

        SET @legacy_roles_expr = IF(legacy_has_roles = 1, 'u.`roles`', 'JSON_ARRAY(''ROLE_USER'')');
        SET @legacy_role_type_expr = IF(legacy_has_role_type = 1, 'NULLIF(TRIM(u.`role_type`), '''')', '''user''');
        SET @legacy_created_expr = IF(legacy_has_created_at = 1, 'u.`created_at`', 'CURRENT_TIMESTAMP');
        SET @legacy_updated_expr = IF(legacy_has_updated_at = 1, 'u.`updated_at`', 'CURRENT_TIMESTAMP');

        IF legacy_has_is_verified = 1 THEN
            SET @legacy_is_verified_expr = 'u.`is_verified`';
        ELSEIF legacy_has_is_active = 1 THEN
            SET @legacy_is_verified_expr = 'u.`is_active`';
        ELSE
            SET @legacy_is_verified_expr = '0';
        END IF;

        SET @legacy_verif_code_expr = IF(legacy_has_verification_code = 1, 'u.`verification_code`', 'NULL');
        SET @legacy_verif_expires_expr = IF(legacy_has_verification_code_expires_at = 1, 'u.`verification_code_expires_at`', 'NULL');
        SET @legacy_reset_code_expr = IF(legacy_has_password_reset_code = 1, 'u.`password_reset_code`', 'NULL');
        SET @legacy_reset_expires_expr = IF(legacy_has_password_reset_code_expires_at = 1, 'u.`password_reset_code_expires_at`', 'NULL');
        SET @legacy_water_expr = IF(legacy_has_water_intake = 1, 'u.`water_intake`', 'NULL');

        SET @merge_sql = CONCAT(
            'INSERT INTO `users` (',
            '`id`, `email`, `name`, `password_hash`, `roles`, `role_type`, `created_at`, `updated_at`, ',
            '`is_verified`, `verification_code`, `verification_code_expires_at`, `password_reset_code`, `password_reset_code_expires_at`, `water_intake`',
            ') SELECT ',
            @legacy_id_expr, ', ',
            'COALESCE(', @legacy_email_expr, ', CONCAT(''unknown+'', ', @legacy_id_expr, ', ''@local.invalid'')), ',
            'COALESCE(', @legacy_name_expr, ', CONCAT(''user_'', ', @legacy_id_expr, ')), ',
            @legacy_password_expr, ', ',
            'COALESCE(', @legacy_roles_expr, ', JSON_ARRAY(''ROLE_USER'')), ',
            'COALESCE(', @legacy_role_type_expr, ', ''user''), ',
            @legacy_created_expr, ', ',
            @legacy_updated_expr, ', ',
            @legacy_is_verified_expr, ', ',
            @legacy_verif_code_expr, ', ',
            @legacy_verif_expires_expr, ', ',
            @legacy_reset_code_expr, ', ',
            @legacy_reset_expires_expr, ', ',
            @legacy_water_expr, ' ',
            'FROM `user` u ',
            'ON DUPLICATE KEY UPDATE ',
            '`email` = COALESCE(NULLIF(`users`.`email`, ''''), VALUES(`email`)), ',
            '`name` = COALESCE(NULLIF(`users`.`name`, ''''), VALUES(`name`)), ',
            '`password_hash` = COALESCE(NULLIF(`users`.`password_hash`, ''''), VALUES(`password_hash`)), ',
            '`roles` = COALESCE(`users`.`roles`, VALUES(`roles`)), ',
            '`role_type` = COALESCE(NULLIF(`users`.`role_type`, ''''), VALUES(`role_type`)), ',
            '`is_verified` = CASE WHEN `users`.`is_verified` = 1 THEN 1 ELSE VALUES(`is_verified`) END, ',
            '`verification_code` = COALESCE(`users`.`verification_code`, VALUES(`verification_code`)), ',
            '`verification_code_expires_at` = COALESCE(`users`.`verification_code_expires_at`, VALUES(`verification_code_expires_at`)), ',
            '`password_reset_code` = COALESCE(`users`.`password_reset_code`, VALUES(`password_reset_code`)), ',
            '`password_reset_code_expires_at` = COALESCE(`users`.`password_reset_code_expires_at`, VALUES(`password_reset_code_expires_at`)), ',
            '`water_intake` = COALESCE(`users`.`water_intake`, VALUES(`water_intake`))'
        );

        PREPARE stmt_merge FROM @merge_sql;
        EXECUTE stmt_merge;
        DEALLOCATE PREPARE stmt_merge;
    END IF;

    -- 5) Index unique email (seulement si aucune duplication)
    SELECT COUNT(*) INTO duplicate_email_count
    FROM (
        SELECT `email`
        FROM `users`
        WHERE `email` IS NOT NULL AND TRIM(`email`) <> ''
        GROUP BY `email`
        HAVING COUNT(*) > 1
    ) AS d;

    SELECT COUNT(*) INTO idx_email_exists
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND index_name = 'uk_users_email';

    IF idx_email_exists = 0 AND duplicate_email_count = 0 THEN
        ALTER TABLE `users` ADD CONSTRAINT `uk_users_email` UNIQUE (`email`);
    END IF;

    -- 6) Index de support
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND index_name = 'idx_users_created_at'
    ) THEN
        CREATE INDEX `idx_users_created_at` ON `users` (`created_at`);
    END IF;

    -- 7) Recaler l'auto increment sur le max(id)+1
    SET @next_ai = (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `users`);
    SET @ai_sql = CONCAT('ALTER TABLE `users` AUTO_INCREMENT = ', @next_ai);
    PREPARE stmt_ai FROM @ai_sql;
    EXECUTE stmt_ai;
    DEALLOCATE PREPARE stmt_ai;
END$$

DELIMITER ;

CALL `sp_normalize_users`();
DROP PROCEDURE IF EXISTS `sp_normalize_users`;

