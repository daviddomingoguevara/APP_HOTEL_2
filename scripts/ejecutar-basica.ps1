# ============================================================
#  Ejecuta la aplicacion JavaFX basica (version A).
#  Uso:  .\scripts\ejecutar-basica.ps1
# ============================================================
. "$PSScriptRoot\maven-comun.ps1"

Write-Host ""
Write-Host "Abriendo la aplicacion BASICA (version A)..." -ForegroundColor Cyan
Write-Host "Si es la primera vez, Maven descargara JavaFX y las dependencias." -ForegroundColor DarkGray

Invoke-MavenEnProyecto -Proyecto "aplicacion-basica" -Argumentos "-B", "-ntp", "javafx:run"
