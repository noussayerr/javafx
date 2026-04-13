<?php
// Script pour afficher la structure de la base de données existante

$host = 'localhost';
$user = 'root';
$password = '';
$database = 'pi_projet';

// Se connecter à MySQL
$mysqli = new mysqli($host, $user, $password, $database);

if ($mysqli->connect_error) {
    die("Erreur de connexion: " . $mysqli->connect_error);
}

echo "<h1>📊 Structure de la base de données : pi_projet</h1>";
echo "<hr>";

// Récupérer toutes les tables
$result = $mysqli->query("SHOW TABLES");

while ($table = $result->fetch_row()) {
    $tableName = $table[0];

    echo "<h2>📋 Table: <strong>$tableName</strong></h2>";
    echo "<table border='1' cellpadding='10' style='width:100%; margin-bottom:20px;'>";
    echo "<tr style='background-color:#f0f0f0;'>";
    echo "<th>Colonne</th><th>Type</th><th>Null</th><th>Clé</th><th>Default</th><th>Extra</th>";
    echo "</tr>";

    // Récupérer la structure de la table
    $describeResult = $mysqli->query("DESC $tableName");

    while ($column = $describeResult->fetch_assoc()) {
        echo "<tr>";
        echo "<td><strong>" . $column['Field'] . "</strong></td>";
        echo "<td>" . $column['Type'] . "</td>";
        echo "<td>" . $column['Null'] . "</td>";
        echo "<td>" . ($column['Key'] ?: '-') . "</td>";
        echo "<td>" . ($column['Default'] ?: '-') . "</td>";
        echo "<td>" . ($column['Extra'] ?: '-') . "</td>";
        echo "</tr>";
    }

    echo "</table>";

    // Afficher aussi les relations (clés étrangères) si disponibles
    echo "<h3>🔗 Relations de la table: $tableName</h3>";

    $fkResult = $mysqli->query("
        SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
        FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
        WHERE TABLE_NAME = '$tableName' AND REFERENCED_TABLE_NAME IS NOT NULL
    ");

    if ($fkResult->num_rows > 0) {
        echo "<ul>";
        while ($fk = $fkResult->fetch_assoc()) {
            echo "<li><strong>" . $fk['COLUMN_NAME'] . "</strong> → " . $fk['REFERENCED_TABLE_NAME'] . "." . $fk['REFERENCED_COLUMN_NAME'] . "</li>";
        }
        echo "</ul>";
    } else {
        echo "<p>Aucune clé étrangère.</p>";
    }

    echo "<hr>";
}

// Afficher aussi le nombre de lignes de chaque table
echo "<h2>📊 Nombre d'enregistrements</h2>";
echo "<table border='1' cellpadding='10'>";
echo "<tr style='background-color:#f0f0f0;'><th>Table</th><th>Nombre d'enregistrements</th></tr>";

$result = $mysqli->query("SHOW TABLES");
while ($table = $result->fetch_row()) {
    $tableName = $table[0];
    $countResult = $mysqli->query("SELECT COUNT(*) as count FROM $tableName");
    $countRow = $countResult->fetch_assoc();
    echo "<tr><td><strong>$tableName</strong></td><td>" . $countRow['count'] . "</td></tr>";
}
echo "</table>";

$mysqli->close();
?>
<style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    table { border-collapse: collapse; }
    td, th { padding: 10px; text-align: left; border: 1px solid #ddd; }
    h1 { color: #333; }
    h2 { color: #666; margin-top: 30px; }
    h3 { color: #999; }
</style>

