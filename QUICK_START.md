# 🚀 DÉMARRAGE RAPIDE - E-Sport Forum

## ⚙️ Prérequis

Avant de lancer l'application, assurez-vous d'avoir :

1. **JDK 17+** (pas JRE!) - [Télécharger ici](https://www.oracle.com/java/technologies/downloads/)
2. **Maven 3.8+** - [Télécharger ici](https://maven.apache.org/download.cgi)
3. **MySQL 8.0+** avec XAMPP
4. **Git** (optionnel)

## ✅ Étape 1 : Vérifier l'Installation

```powershell
# Vérifier Java
java -version
# Doit afficher: java version "17.x.x" ou supérieur

# Vérifier Maven
mvn -version
# Doit afficher: Apache Maven 3.8+
```

## 🗄️ Étape 2 : Configurer la Base de Données

### Option A : Avec phpMyAdmin (XAMPP)

1. Ouvrir `http://localhost/phpmyadmin/`
2. Aller dans l'onglet SQL
3. Copier-coller le contenu de `database.sql`
4. Exécuter (Ctrl + Entrée)

### Option B : Via ligne de commande

```bash
# Accéder à MySQL
mysql -u root -p

# Exécuter le script
SOURCE C:/xampp1/htdocs/forum/database.sql;

# Vérifier la création
SHOW DATABASES;
USE pi_projet;
SHOW TABLES;
SELECT * FROM posts;
```

## 📦 Étape 3 : Compiler le Projet

```bash
cd C:\xampp1\htdocs\forum

# Compiler et installer les dépendances
mvn clean install -DskipTests
```

**Ou utilisez le script batch :**
```bash
start.bat
```

## 🚀 Étape 4 : Démarrer l'Application

```bash
cd C:\xampp1\htdocs\forum
mvn spring-boot:run
```

L'application démarre sur :
- **API REST** : http://localhost:8080/api/posts
- **Interface Desktop** : Se lance automatiquement

## 🧪 Étape 5 : Tester l'Application

### Tester via Postman ou cURL

```bash
# Créer un post
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Mon premier post\",\"content\":\"Contenu intéressant\",\"authorName\":\"Jean\"}"

# Récupérer tous les posts
curl http://localhost:8080/api/posts

# Récupérer un post spécifique
curl http://localhost:8080/api/posts/1

# Mettre à jour un post
curl -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Titre modifié\",\"content\":\"Contenu modifié\",\"authorName\":\"Jean\"}"

# Supprimer un post
curl -X DELETE http://localhost:8080/api/posts/1
```

## 📱 Interface Desktop JavaFX

L'application desktop s'ouvre automatiquement avec :
- **Formulaire** à gauche pour ajouter/modifier
- **Tableau** à droite pour voir tous les posts
- **Moteur de recherche** pour filtrer par titre

## ⚡ Commandes Utiles

```bash
# Compiler sans tests
mvn clean compile

# Exécuter les tests
mvn test

# Créer un JAR exécutable
mvn package

# Nettoyer le dossier target
mvn clean

# Voir les dépendances
mvn dependency:tree
```

## 🔧 Configuration

### Modifier la base de données

Éditer `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://root:@127.0.0.1:3306/pi_projet?serverVersion=8.0.32&charset=utf8mb4
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

### Modifier le port

```properties
server.port=8080
```

## 🐛 Troubleshooting

### Erreur : "No compiler is provided"
- **Solution** : Installer un JDK (pas JRE) et configurer JAVA_HOME

### Erreur : "mvn: command not found"
- **Solution** : Ajouter Maven au PATH

### Erreur : "Connection refused" (MySQL)
- **Solution** : Vérifier que MySQL/XAMPP est démarré
- Vérifier la connexion : `mysql -u root`

### Erreur : "No database selected"
- **Solution** : Exécuter le script `database.sql` dans MySQL

### Port 8080 déjà utilisé
- **Solution** : Modifier le port dans `application.properties`

## 📚 Fichiers Importants

- `pom.xml` - Configuration Maven
- `src/main/resources/application.properties` - Configuration Spring
- `database.sql` - Script de base de données
- `README.md` - Documentation complète
- `PROJET_RESUME.md` - Résumé du projet

## 🎯 Prochaines Fonctionnalités

- [ ] CRUD Commentaires
- [ ] Authentification utilisateur
- [ ] Système de rôles
- [ ] Pagination
- [ ] Notifications en temps réel

## 📞 Support

Pour plus d'aide :
1. Consultez `README.md`
2. Vérifiez `PROJET_RESUME.md`
3. Lisez les logs d'erreur Maven

---

**Status** : ✅ Prêt à être lancé !

