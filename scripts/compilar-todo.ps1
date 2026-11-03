# ============================================================
#  Compila las dos aplicaciones, ejecuta sus tests y comprueba
#  que el servidor se puede compilar con javac.
#  Uso:  .\scripts\compilar-todo.ps1
# ============================================================
. "$PSScriptRoot\maven-comun.ps1"

Invoke-MavenEnProyecto -Proyecto "aplicacion-basica" -Argumentos "-B", "-ntp", "clean", "test"
Invoke-MavenEnProyecto -Proyecto "aplicacion-mejorada" -Argumentos "-B", "-ntp", "clean", "test"

$java = Get-Command java -ErrorAction SilentlyContinue
$javac = Get-Command javac -ErrorAction SilentlyContinue
if (-not $javac) {
    throw "No se encontro javac. Instala un JDK (no solo el JRE) y comprueba el PATH."
}

$salida = Join-Path $script:RaizTrabajo ".tools\clases-servidor"
New-Item -ItemType Directory -Force -Path $salida | Out-Null
Write-Host ""
Write-Host "==> servidor" -ForegroundColor Green
& $javac.Source -encoding UTF-8 -d $salida "servidor\ServidorMetricas.java"
if ($LASTEXITCODE -ne 0) {
    throw "El servidor no compilo correctamente."
}

Write-Host ""
Write-Host "Todo compilado y probado correctamente." -ForegroundColor Green
Write-Host "  - aplicacion-basica: tests OK" -ForegroundColor Green
Write-Host "  - aplicacion-mejorada: tests OK" -ForegroundColor Green
Write-Host "  - servidor: compilacion OK" -ForegroundColor Green
Write-Host ""
Write-Host "Siguiente paso: .\scripts\iniciar-servidor.ps1" -ForegroundColor Cyan
