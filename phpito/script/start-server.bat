@echo off
set ADDRESS=%1
set DIRECTORY=%2

echo ""
echo "[*] PHPito starting server at %date% %time%"
echo "[*] Starting PHP server on: %ADDRESS%"
echo "[*] Directory root: %DIRECTORY%"

@echo on
cd %directory% && php -S %address%