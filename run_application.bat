@echo off
REM Script pour démarrer l'application E-Sport Forum

echo.
echo =========================================
echo  E-SPORT FORUM - Gestion de l'Application
echo =========================================
echo.

REM Définir les variables d'environnement
set JAVA_HOME=C:\Program Files\Java\jdk-17
set MAVEN_HOME=C:\maven
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo [1] Vérification des dépendances...
echo.

REM Vérifier Java
echo Vérification de Java...
java -version
if %ERRORLEVEL% neq 0 (
    echo ERREUR: Java n'est pas correctement installé!
    pause
    exit /b 1
)

REM Vérifier Maven
echo.
echo Vérification de Maven...
mvn --version
if %ERRORLEVEL% neq 0 (
    echo ERREUR: Maven n'est pas correctement installé!
    pause
    exit /b 1
)

echo.
echo =========================================
echo [2] Compilation du projet...
echo =========================================
echo.

cd /d "%~dp0"
mvn clean compile
if %ERRORLEVEL% neq 0 (
    echo ERREUR: La compilation a échoué!
    pause
    exit /b 1
)

echo.
echo =========================================
echo Compilation réussie!
echo =========================================
echo.
echo Options:
echo [1] Démarrer le serveur Backend (Spring Boot)
echo [2] Démarrer l'interface Frontend (JavaFX)
echo [3] Démarrer les deux (nécessite 2 fenêtres)
echo [4] Quitter
echo.

setlocal enabledelayedexpansion
set /p choice="Sélectionnez une option (1-4): "

if "%choice%"=="1" (
    echo Démarrage du serveur Spring Boot...
    mvn spring-boot:run
) else if "%choice%"=="2" (
    echo Démarrage de l'interface JavaFX...
    mvn javafx:run
) else if "%choice%"=="3" (
    echo Démarrage du serveur Spring Boot dans une nouvelle fenêtre...
    start "Spring Boot Server" cmd /k "cd /d %~dp0 && set JAVA_HOME=C:\Program Files\Java\jdk-17 && set MAVEN_HOME=C:\maven && set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%%PATH%% && mvn spring-boot:run"
    timeout /t 3 /nobreak
    echo Démarrage de l'interface JavaFX...
    mvn javafx:run
) else (
    echo Opération annulée.
)

pause

