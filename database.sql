-- Script d'initialisation de la base de données pour E-Sport Forum

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS pi_projet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pi_projet;

-- Table des Posts
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    author_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_title (title),
    INDEX idx_author (author_name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insérer des données de test
INSERT INTO posts (title, content, author_name) VALUES
('Bienvenue sur le forum E-Sport', 'Ceci est le premier post du forum. Bienvenue à tous les amateurs de salle de sport!', 'Admin'),
('Conseils pour débuter', 'Voici quelques conseils pour bien débuter à la salle de sport...', 'John Doe'),
('Programme d\'entraînement', 'Découvrez mon programme d\'entraînement complet pour les débutants.', 'Jane Smith');

-- Table des Commentaires
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content LONGTEXT NOT NULL,
    author_name VARCHAR(100),
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_author (author_name),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insérer des données de test pour les commentaires
INSERT INTO comments (content, author_name, post_id) VALUES
('Excellente initiative! Merci pour le partage.', 'Marie', 1),
('J''ai une question: comment bien débuter?', 'Paul', 1),
('Super conseils, j''ai commencé aujourd''hui!', 'Sophie', 2),
('Pouvez-vous développer sur la récupération?', 'Thomas', 2);

-- Afficher le contenu
SELECT * FROM posts;
SELECT * FROM comments;

