# 🚀 EXÉCUTER LA BASE DE DONNÉES - GUIDE RAPIDE

## ✅ OPTION 1 : Avec PHP (PLUS SIMPLE) ⭐

### Étapes :

1. **Démarrer XAMPP**
   - Ouvrir le panneau de contrôle XAMPP
   - Cliquer "Start" pour **Apache** et **MySQL**

2. **Accéder au script PHP**
   - Ouvrir votre navigateur
   - Aller à : `http://localhost/forum/setup_db.php`

3. **Attendre quelques secondes**
   - Le script va exécuter automatiquement le SQL
   - Vous verrez : ✅ Base de données créée avec succès!

4. **Vérifier dans phpMyAdmin**
   - Aller à : `http://localhost/phpmyadmin/`
   - Cliquer sur "pi_projet"
   - Vous devez voir : tables "posts" et "comments"

---

## ✅ OPTION 2 : Via phpMyAdmin

### Étapes :

1. **Ouvrir phpMyAdmin**
   - http://localhost/phpmyadmin/

2. **Cliquer sur "Importer"**

3. **Sélectionner le fichier**
   - Parcourir : `C:\xampp1\htdocs\forum\database.sql`

4. **Cliquer "Exécuter"**

5. **Vérifier le résultat**
   - Vous devez voir un message de succès

---

## ✅ OPTION 3 : Avec le script Batch

### Étapes :

1. **Double-cliquer sur**
   - `C:\xampp1\htdocs\forum\run_database.bat`

2. **Quand il demande le mot de passe**
   - Appuyer sur Entrée (aucun mot de passe par défaut)

3. **Attendre que le script se termine**

4. **Fermer la fenêtre**

---

## ✅ VÉRIFIER QUE TOUT FONCTIONNE

Après avoir exécuté, ouvrez phpMyAdmin et vérifiez :

1. Base de données `pi_projet` existe
2. Table `posts` contient 3 posts
3. Table `comments` contient 4 commentaires

### Via la ligne de commande :

```powershell
cd "C:\xampp1\mysql\bin"
mysql -u root
```

```sql
USE pi_projet;
SELECT * FROM posts;
SELECT * FROM comments;
```

---

## 🎯 MÉTHODE RECOMMANDÉE

**Je recommande l'OPTION 1 (PHP)** car :
- ✅ Le plus simple
- ✅ Pas besoin de ligne de commande
- ✅ Voir directement le résultat
- ✅ Vérification automatique

---

## 🚀 APRÈS LA BD

Une fois la base de données créée :

1. **Compiler l'application**
```bash
cd C:\xampp1\htdocs\forum
mvn clean install -DskipTests
```

2. **Démarrer l'application**
```bash
mvn spring-boot:run
```

3. **L'application utilisera la BD existante** ✅

---

**Quelle option souhaitez-vous utiliser ?** 
Dites-moi et je vous aiderai!

