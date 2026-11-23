@echo off
rem Compila e executa o sistema de futebol (precisa do JDK 17 ou mais novo).
rem Rode este arquivo de dentro da pasta "Entrega 3".
chcp 65001 >nul

if not exist lib\mysql-connector-j-*.jar (
    echo Falta o driver do MySQL. Veja o passo a passo em lib\LEIAME.txt.
    exit /b 1
)

if not exist out mkdir out
javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d out src\futebol\Main.java
if errorlevel 1 exit /b 1

java -Dfile.encoding=UTF-8 -cp "out;lib/*" futebol.Main
