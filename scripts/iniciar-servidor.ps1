# ============================================================
#  Inicia el servidor local de metricas y la web del experimento.
#  Uso:  .\scripts\iniciar-servidor.ps1 [-Puerto 8080]
# ============================================================
param(
    [int]$Puerto = 8080
)

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
Set-Location $raiz

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    throw "No se encontro Java. Instala JDK 21 o superior y comprueba que 'java' esta en el PATH."
}

Write-Host ""
Write-Host "=========================================================" -ForegroundColor Green
Write-Host " Servidor de metricas - TRABAJO 2" -ForegroundColor Green
Write-Host " Web:  http://localhost:$Puerto/" -ForegroundColor Green
Write-Host " API:  http://localhost:$Puerto/api/salud" -ForegroundColor Green
Write-Host " Datos: $(Join-Path $raiz 'datos')" -ForegroundColor DarkGray
Write-Host "=========================================================" -ForegroundColor Green
Write-Host "Pulsa Ctrl+C para detener el servidor." -ForegroundColor DarkGray
Write-Host ""

& $java.Source "servidor\ServidorMetricas.java" --puerto $Puerto --web "web" --datos "datos"
if ($LASTEXITCODE -ne 0) {
    throw "El servidor termino con codigo $LASTEXITCODE. Comprueba que el puerto $Puerto esta libre."
}
