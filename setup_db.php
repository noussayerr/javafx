<?php
// Script PHP pour exécuter le SQL et créer la base de données

$host = 'localhost';
$user = 'root';
$password = '';
$database = 'pi_projet';

// Lire le fichier SQL
$sqlFile = __DIR__ . '/database.sql';

if (!file_exists($sqlFile)) {
    die("Erreur: Le fichier database.sql n'existe pas!");
}

$sqlContent = file_get_contents($sqlFile);

// Se connecter à MySQL (sans spécifier la BD d'abord)
$mysqli = new mysqli($host, $user, $password);

if ($mysqli->connect_error) {
    die("Erreur de connexion: " . $mysqli->connect_error);
}

// Exécuter le contenu du fichier SQL
// Diviser par les points-virgules pour exécuter plusieurs instructions
$statements = array_filter(array_map('trim', explode(';', $sqlContent)));

foreach ($statements as $statement) {
    if (!empty($statement)) {
        echo "Exécution: " . substr($statement, 0, 60) . "...<br>";

        if (!$mysqli->query($statement)) {
            echo "❌ Erreur: " . $mysqli->error . "<br>";
        } else {
            echo "✅ OK<br>";
        }
    }
}

$mysqli->close();

echo "<hr>";
echo "<h2>✅ Base de données créée avec succès!</h2>";
echo "<p><a href='http://localhost/phpmyadmin/'>Voir dans phpMyAdmin</a></p>";
?>

