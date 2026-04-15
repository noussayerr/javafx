# Forum1 - Gestion MVC des postes

Cette application JavaFX implemente un module MVC pour l'entite `Poste` avec les champs:

- `id`
- `titre`
- `description`
- `date_creation`

Entite `Commentaire` liee a `Poste`:

- `id`
- `poste_id`
- `contenu`
- `created_at`

Fonctionnalites disponibles:

- Ajouter un poste
- Afficher la liste des postes
- Modifier un poste selectionne
- Supprimer un poste avec confirmation
- Afficher les commentaires lies au poste selectionne dans un second tableau
- Ajouter, modifier et supprimer les commentaires du poste selectionne
- Validations serveur (titre/description obligatoires, longueur minimale du titre)

## Configuration DB

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/pi_projet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8"
$env:DB_USER="root"
$env:DB_PASSWORD=""
```

## Initialisation / verification schema (optionnel)

```powershell
Set-Location "C:\xampp\htdocs\Forum1"
mysql -u root -h 127.0.0.1 < ".\src\main\resources\db\mysql\init_pi_projet.sql"
mysql -u root -h 127.0.0.1 < ".\src\main\resources\db\mysql\verify_pi_projet.sql"
```

## Lancer l'application

```powershell
Set-Location "C:\xampp\htdocs\Forum1"
.\mvnw.cmd clean javafx:run
```

## Architecture

- Modele: `src/main/java/javafx/forum1/model/Poste.java`
- Vue: `src/main/resources/javafx/forum1/hello-view.fxml`
- Controleur: `src/main/java/javafx/forum1/controller/PosteController.java`
- Service: `src/main/java/javafx/forum1/service/PosteService.java`
- DAO JDBC: `src/main/java/javafx/forum1/dao/PosteDao.java`
- Service commentaires: `src/main/java/javafx/forum1/service/CommentaireService.java`
- DAO commentaires: `src/main/java/javafx/forum1/dao/CommentaireDao.java`

