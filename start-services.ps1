$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Starte Infrastruktur (PostgreSQL + Mosquitto)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    '-NoExit',
    '-Command',
    "Set-Location '$projectRoot'; docker compose up -d"
)

Start-Sleep -Seconds 5

Write-Host "Starte studentenwerk_simulator (Port 8081)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    '-NoExit',
    '-Command',
    "Set-Location '$projectRoot'; .\mvnw.cmd -pl studentenwerk_simulator spring-boot:run"
)

Write-Host "Starte mensa_app_backend (Port 8082)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    '-NoExit',
    '-Command',
    "Set-Location '$projectRoot'; .\mvnw.cmd -pl mensa_app_backend spring-boot:run"
)
