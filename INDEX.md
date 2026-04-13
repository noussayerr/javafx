# 📚 INDEX DU PROJET E-SPORT FORUM

## 📖 Documentation

| Fichier | Description |
|---------|-------------|
| **QUICK_START.md** | 🚀 Démarrage rapide (lisez celui-ci en premier!) |
| **README.md** | 📚 Documentation complète et détaillée |
| **PROJET_RESUME.md** | 📋 Résumé de la structure du projet |
| **INDEX.md** | 📑 Ce fichier - guide de navigation |

## 🏗️ Structure du Code

### Entity (Modèles de données)
```
src/main/java/spring/boot/entity/
├── Post.java          ← Entité Post (CRUD implémenté)
└── Comment.java       ← Entité Comment (CRUD implémenté)
```

### Repository (Accès aux données)
```
src/main/java/spring/boot/repository/
├── PostRepository.java     ← Repository JPA pour Post
└── CommentRepository.java  ← Repository JPA pour Comment
```

### Service (Logique métier)
```
src/main/java/spring/boot/service/
├── PostService.java    ← Logique métier pour Post
└── CommentService.java ← Logique métier pour Comment
```

### Controller (API REST)
```
src/main/java/spring/boot/controller/
├── PostController.java    ← API REST /api/posts
└── CommentController.java ← API REST /api/comments
```

### DTO (Transfert de données)
```
src/main/java/spring/boot/dto/
├── PostDTO.java    ← Objet de transfert Post
└── CommentDTO.java ← Objet de transfert Comment
```

### Exception (Gestion des erreurs)
```
src/main/java/spring/boot/exception/
├── ResourceNotFoundException.java  ← Exception personnalisée
└── GlobalExceptionHandler.java     ← Gestionnaire global
```

### UI (Interface Desktop)
```
src/main/java/spring/boot/ui/
├── ForumApplication.java           ← Application JavaFX
└── controller/
    └── PostUIController.java       ← Contrôleur interface
```

### Resources (Fichiers de configuration)
```
src/main/resources/
├── application.properties          ← Config Spring Boot & MySQL
└── spring/boot/ui/views/
    └── post.fxml                   ← Interface JavaFX
```

## 🚀 Commandes Essentielles

```bash
# Compilation
mvn clean install -DskipTests

# Démarrage
mvn spring-boot:run

# Tests
mvn test

# Package JAR
mvn package

# Nettoyer
mvn clean
```

## 🌐 Endpoints REST

### Posts
```
POST   /api/posts                      ← Créer
GET    /api/posts                      ← Tous les posts
GET    /api/posts/{id}                 ← Post spécifique
PUT    /api/posts/{id}                 ← Modifier
DELETE /api/posts/{id}                 ← Supprimer
GET    /api/posts/search/title         ← Recherche par titre
GET    /api/posts/search/author        ← Recherche par auteur
```

### Commentaires
```
POST   /api/comments                   ← Créer
GET    /api/comments                   ← Tous les commentaires
GET    /api/comments/{id}              ← Commentaire spécifique
GET    /api/comments/post/{postId}     ← Commentaires d'un post
PUT    /api/comments/{id}              ← Modifier
DELETE /api/comments/{id}              ← Supprimer
DELETE /api/comments/post/{postId}     ← Supprimer tous les commentaires d'un post
GET    /api/comments/search/author     ← Recherche par auteur
```

## 🗄️ Schéma Base de Données

### Table: posts
| Colonne | Type | Notes |
|---------|------|-------|
| id | BIGINT | PK, AUTO_INCREMENT |
| title | VARCHAR(255) | NOT NULL |
| content | LONGTEXT | - |
| author_name | VARCHAR(100) | - |
| created_at | TIMESTAMP | DEFAULT NOW() |
| updated_at | TIMESTAMP | ON UPDATE NOW() |

### Table: comments
| Colonne | Type | Notes |
|---------|------|-------|
| id | BIGINT | PK, AUTO_INCREMENT |
| content | LONGTEXT | NOT NULL |
| author_name | VARCHAR(100) | - |
| post_id | BIGINT | FK → posts.id |
| created_at | TIMESTAMP | DEFAULT NOW() |
| updated_at | TIMESTAMP | ON UPDATE NOW() |

## 📦 Dépendances Principales

| Dépendance | Version | Utilité |
|------------|---------|---------|
| Spring Boot | 3.2.4 | Framework principal |
| Spring Data JPA | 3.2.4 | ORM & accès données |
| MySQL Connector | 8.0.33 | Driver MySQL |
| JavaFX | 21 | Interface Desktop |
| Lombok | Latest | Annotations pour réduire code |
| JUnit 5 | Latest | Tests unitaires |

## 🎯 Fonctionnalités Implémentées

✅ **CRUD Posts**
- Créer, Lire, Modifier, Supprimer
- Recherche par titre et auteur

✅ **CRUD Commentaires**
- Créer, Lire, Modifier, Supprimer
- Récupérer les commentaires d'un post
- Supprimer tous les commentaires d'un post
- Recherche par auteur

✅ **API REST**
- Architecture RESTful
- CORS activé
- Gestion des erreurs globale

✅ **Interface Desktop**
- Tableau des posts
- Formulaire ajouter/modifier
- Moteur de recherche
- Gestion complète des posts

## ⚙️ Configuration

### Base de Données
```properties
spring.datasource.url=jdbc:mysql://root:@127.0.0.1:3306/pi_projet?serverVersion=8.0.32&charset=utf8mb4
spring.datasource.username=root
spring.datasource.password=
```

### Hibernate
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Port
```properties
server.port=8080
```

## 🔗 Liens Utiles

- [Spring Boot Doc](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [JavaFX Doc](https://openjfx.io/)
- [MySQL Doc](https://dev.mysql.com/doc/)
- [Maven Doc](https://maven.apache.org/guides/)

## 📝 Fichiers Configuration

| Fichier | Description |
|---------|-------------|
| pom.xml | Configuration Maven |
| application.properties | Configuration Spring Boot |
| database.sql | Script création BD |
| start.bat | Script de démarrage Windows |

## 🧪 Test du Projet

### 1. Vérifier la BD
```bash
mysql -u root -p
USE pi_projet;
SELECT * FROM posts;
SELECT * FROM comments;
```

### 2. Tester l'API
```bash
curl http://localhost:8080/api/posts
curl http://localhost:8080/api/comments
```

### 3. Interface Desktop
- L'interface doit se lancer automatiquement
- Ajouter/Modifier/Supprimer des posts
- Effectuer des recherches

## 🚦 Checklist de Démarrage

- [ ] JDK 17+ installé
- [ ] Maven 3.8+ installé
- [ ] MySQL/XAMPP démarré
- [ ] database.sql exécuté
- [ ] `mvn clean install` réussi
- [ ] `mvn spring-boot:run` démarré
- [ ] Interface s'affiche
- [ ] Créer/Modifier/Supprimer fonctionne

## 💡 Prochaines Étapes

1. **Authentification** - Ajouter les rôles utilisateur
2. **Pagination** - Ajouter pour les listes longues
3. **Validations** - Ajouter @Valid & @NotEmpty
4. **Notifications** - Système de notifications en temps réel
5. **Upload fichiers** - Joindre des pièces aux posts
6. **Statistiques** - Tableaux de bord
7. **Tests E2E** - Tests d'intégration complets

---

**Status**: ✅ Projet complètement implémenté et prêt à démarrer

**Dernière mise à jour**: 2026-04-13

