# E-Sport Forum Application

Une application desktop développée avec **Spring Boot** et **JavaFX** pour gérer un forum de salle de sport (e-sport).

## 📋 Fonctionnalités

### CRUD des Posts (Déjà implémenté)
- ✅ **Créer** un nouveau post
- ✅ **Lire** tous les posts
- ✅ **Mettre à jour** un post existant
- ✅ **Supprimer** un post
- ✅ **Rechercher** les posts par titre ou auteur

## 🛠️ Technologies utilisées

- **Java 23**
- **Spring Boot 3.2.4**
- **JavaFX 23** - Interface graphique desktop
- **JPA/Hibernate** - ORM (Object-Relational Mapping)
- **MySQL 8.0.32** - Base de données
- **Maven** - Gestionnaire de projet

## 📦 Structure du projet

```
forum/
├── src/
│   ├── main/
│   │   ├── java/spring/boot/
│   │   │   ├── Main.java                    # Point d'entrée Spring Boot
│   │   │   ├── entity/
│   │   │   │   └── Post.java                # Entité Post
│   │   │   ├── repository/
│   │   │   │   └── PostRepository.java      # Accès aux données
│   │   │   ├── service/
│   │   │   │   └── PostService.java         # Logique métier
│   │   │   ├── controller/
│   │   │   │   └── PostController.java      # API REST
│   │   │   ├── ui/
│   │   │   │   ├── ForumApplication.java    # Démarrage JavaFX
│   │   │   │   └── controller/
│   │   │   │       └── PostUIController.java# Contrôleur JavaFX
│   │   │   ├── dto/
│   │   │   │   └── PostDTO.java             # Objet de transfert de données
│   │   │   └── exception/
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties       # Configuration
│   │       └── spring/boot/ui/views/
│   │           └── post.fxml                # Interface FXML
│   └── test/
│       └── java/spring/boot/service/
│           └── PostServiceTest.java         # Tests unitaires
└── pom.xml                                   # Configuration Maven
```

## 🚀 Installation et démarrage

### Prérequis
- JDK 23 ou supérieur
- Maven 3.8+
- MySQL 8.0.32+

### Configuration de la base de données

1. **Créer la base de données** :
```sql
CREATE DATABASE pi_projet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Vérifier la connection** dans `application.properties` :
```properties
spring.datasource.url=jdbc:mysql://root:@127.0.0.1:3306/pi_projet?serverVersion=8.0.32&charset=utf8mb4
spring.datasource.username=root
spring.datasource.password=
```

### Démarrage de l'application

1. **Compiler le projet** :
```bash
mvn clean install
```

2. **Exécuter l'application** :
```bash
mvn spring-boot:run
```

Ou directement depuis l'IDE en exécutant `Main.java`

## 📱 Utilisation de l'interface

### Interface JavaFX

L'application propose une interface graphique divisée en deux zones :

**Zone gauche (Formulaire)** :
- Champs pour ajouter/modifier un post
- Titre, Contenu, Auteur
- Boutons : Ajouter, Modifier, Supprimer, Effacer

**Zone droite (Tableau)** :
- Liste de tous les posts
- Moteur de recherche par titre
- Sélectionner un post pour le modifier

### API REST

L'API REST est disponible sur `http://localhost:8080/api/posts`

#### Endpoints disponibles :

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/posts` | Créer un post |
| GET | `/api/posts` | Récupérer tous les posts |
| GET | `/api/posts/{id}` | Récupérer un post par ID |
| PUT | `/api/posts/{id}` | Mettre à jour un post |
| DELETE | `/api/posts/{id}` | Supprimer un post |
| GET | `/api/posts/search/title?title=xxx` | Rechercher par titre |
| GET | `/api/posts/search/author?authorName=xxx` | Rechercher par auteur |

## 📝 Exemple d'utilisation de l'API

### Créer un post
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Premier Post",
    "content": "Contenu du post",
    "authorName": "John Doe"
  }'
```

### Récupérer tous les posts
```bash
curl http://localhost:8080/api/posts
```

### Mettre à jour un post
```bash
curl -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Post modifié",
    "content": "Nouveau contenu",
    "authorName": "Jane Doe"
  }'
```

### Supprimer un post
```bash
curl -X DELETE http://localhost:8080/api/posts/1
```

## 🧪 Tests

Pour exécuter les tests unitaires :
```bash
mvn test
```

## 📚 Prochaines étapes

- [ ] Implémenter le CRUD pour les commentaires
- [ ] Ajouter l'authentification/autorisation
- [ ] Implémenter les rôles utilisateur
- [ ] Ajouter la pagination
- [ ] Ajouter les validations côté client
- [ ] Implémenter les notifications
- [ ] Ajouter des tests d'intégration

## 📧 Support

Pour toute question ou problème, consultez la documentation Spring Boot :
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JavaFX Documentation](https://gluonhq.com/products/javafx/)
- [Hibernate Documentation](https://hibernate.org/)

## 📄 Licence

Ce projet est sous licence MIT.

