# E-Sport Forum Application

Application de gestion de forum pour une communauté de salle de sport (E-Sport), développée avec **Spring Boot** et **JavaFX**.

## Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.4
- **Base de données**: MySQL 8.0.32
- **ORM**: Hibernate (JPA)
- **API REST**: RESTful API avec Spring Web

### Frontend (JavaFX)
- **Version**: JavaFX 21
- **Interface**: FXML
- **Client HTTP**: Java HttpClient

## Structure du Projet

```
forum/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── spring/boot/
│   │   │       ├── Main.java (Démarrage Spring Boot)
│   │   │       ├── controller/
│   │   │       │   ├── PostController.java (REST API Posts)
│   │   │       │   └── CommentController.java (REST API Commentaires)
│   │   │       ├── entity/
│   │   │       │   ├── Post.java
│   │   │       │   └── Comment.java
│   │   │       ├── repository/
│   │   │       │   ├── PostRepository.java
│   │   │       │   └── CommentRepository.java
│   │   │       ├── service/
│   │   │       │   ├── PostService.java
│   │   │       │   ├── CommentService.java
│   │   │       │   ├── HttpClientService.java
│   │   │       │   └── JsonService.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── ResourceNotFoundException.java
│   │   │       ├── dto/
│   │   │       │   ├── PostDTO.java
│   │   │       │   └── CommentDTO.java
│   │   │       └── ui/
│   │   │           ├── ForumApplication.java (Démarrage JavaFX)
│   │   │           ├── controller/
│   │   │           │   └── PostUIController.java
│   │   │           └── views/
│   │   │               └── post.fxml
│   │   └── resources/
│   │       ├── application.properties
│   │       └── spring/boot/ui/views/post.fxml
│   └── test/
│       └── java/
│           └── spring/boot/service/
│               └── PostServiceTest.java
└── pom.xml (Configuration Maven)
```

## Fonctionnalités

### CRUD Posts
- **CREATE**: Créer un nouveau post
- **READ**: Consulter tous les posts ou un post spécifique
- **UPDATE**: Modifier un post existant
- **DELETE**: Supprimer un post
- **SEARCH**: Rechercher par titre ou auteur

### CRUD Commentaires
- **CREATE**: Ajouter un commentaire à un post
- **READ**: Lister les commentaires
- **UPDATE**: Modifier un commentaire
- **DELETE**: Supprimer un commentaire
- **FILTER**: Filtrer par post ou auteur

## Configuration Base de Données

```properties
spring.datasource.url=jdbc:mysql://root:@127.0.0.1:3306/pi_projet?serverVersion=8.0.32&charset=utf8mb4
spring.datasource.username=root
spring.datasource.password=
```

## Démarrage

### Démarrer le Backend (API REST)
```bash
mvn spring-boot:run
```
L'API sera disponible sur `http://localhost:8080/api`

### Démarrer le Frontend (JavaFX)
```bash
mvn javafx:run
```

## Points Terminaux API

### Posts
- `GET /api/posts` - Récupérer tous les posts
- `POST /api/posts` - Créer un post
- `GET /api/posts/{id}` - Récupérer un post
- `PUT /api/posts/{id}` - Mettre à jour un post
- `DELETE /api/posts/{id}` - Supprimer un post
- `GET /api/posts/search/title?title=...` - Rechercher par titre
- `GET /api/posts/search/author?authorName=...` - Rechercher par auteur

### Commentaires
- `GET /api/comments` - Récupérer tous les commentaires
- `POST /api/comments` - Créer un commentaire
- `GET /api/comments/{id}` - Récupérer un commentaire
- `PUT /api/comments/{id}` - Mettre à jour un commentaire
- `DELETE /api/comments/{id}` - Supprimer un commentaire
- `GET /api/comments/post/{postId}` - Récupérer les commentaires d'un post
- `GET /api/comments/search/author?authorName=...` - Rechercher par auteur

## Technologies

- **Language**: Java 17
- **Build Tool**: Maven 3.9.6
- **Framework**: Spring Boot 3.2.4
- **Database**: MySQL 8.0.32
- **GUI Framework**: JavaFX 21
- **JSON Processing**: GSON 2.10.1
- **Build Plugins**: 
  - Maven Compiler Plugin 3.11.0
  - Spring Boot Maven Plugin
  - JavaFX Maven Plugin

## Auteur

Développé pour une salle de sport E-Sport

## Licence

Propriétaire

