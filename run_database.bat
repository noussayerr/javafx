@echo off
REM Script pour exécuter le SQL dans la base de données pi_projet

REM Aller à XAMPP MySQL
cd C:\xampp1\mysql\bin

REM Exécuter le script
mysql -u root -p < C:\xampp1\htdocs\forum\database.sql

echo.
echo ===================================
echo Script SQL exécuté avec succès!
echo ===================================
echo.
pause

