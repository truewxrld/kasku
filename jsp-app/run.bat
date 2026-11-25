@echo off
cd /d "%~dp0"
if not exist target\dependency call mvn clean package
if errorlevel 1 exit /b 1
java -cp "target/classes;target/dependency/*" id.kasku.Main
