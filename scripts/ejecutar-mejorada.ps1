# ============================================================
#  Ejecuta la aplicacion JavaFX mejorada (version B).
#  Uso:  .\scripts\ejecutar-mejorada.ps1
# ============================================================
. "$PSScriptRoot\maven-comun.ps1"

Write-Host ""
Write-Host "Abriendo la aplicacion MEJORADA (version B)..." -ForegroundColor Cyan
Write-Host "Si es la primera vez, Maven descargara JavaFX y las dependencias." -ForegroundColor DarkGray

Invoke-MavenEnProyecto -Proyecto "aplicacion-mejorada" -Argumentos "-B", "-ntp", "javafx:run"
