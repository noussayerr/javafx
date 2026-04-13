@echo off
REM Script de démarrage pour le projet E-Sport Forum
REM Ce script compile et lance l'application

echo ===============================================
echo E-Sport Forum Application
echo ===============================================
echo.

REM Vérifier que Maven est installé
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERREUR: Maven n'est pas installé ou non configuré dans PATH
    echo Veuillez installer Maven et ajouter son chemin à la variable PATH
    pause
    exit /b 1
)

echo Maven détecté: OK
echo.

REM Vérifier que Java est installé
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERREUR: Java n'est pas installé ou non configuré dans PATH
    echo Veuillez installer un JDK (pas JRE) et configurer JAVA_HOME
    pause
    exit /b 1
)

echo Java détecté: OK
java -version
echo.

REM Nettoyer et compiler
echo Compilation du projet...
call mvn clean install -DskipTests

if %errorlevel% neq 0 (
    echo ERREUR: La compilation a échoué
    pause
    exit /b 1
)

echo.
echo Compilation réussie!
echo.
echo Démarrage de l'application...
call mvn spring-boot:run

pause

