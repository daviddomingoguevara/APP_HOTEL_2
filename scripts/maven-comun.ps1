# ============================================================
#  Utilidades comunes de los scripts del TRABAJO 2.
#  Se carga con:  . "$PSScriptRoot\maven-comun.ps1"
# ============================================================

$ErrorActionPreference = "Stop"

$script:RaizTrabajo = Split-Path -Parent $PSScriptRoot
$script:VersionMaven = "3.9.9"

function Get-MavenEjecutable {
    <#
        Devuelve la ruta a mvn.cmd. Si Maven no esta instalado, lo descarga
        en .tools dentro del propio proyecto. Asi el trabajo funciona en un
        aula sin Maven y sin permisos de administrador.
    #>
    $mvnDelSistema = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnDelSistema) {
        return $mvnDelSistema.Source
    }

    $herramientas = Join-Path $script:RaizTrabajo ".tools"
    $mavenHome = Join-Path $herramientas "apache-maven-$script:VersionMaven"
    $mvn = Join-Path $mavenHome "bin\mvn.cmd"

    if (-not (Test-Path $mvn)) {
        New-Item -ItemType Directory -Force -Path $herramientas | Out-Null
        $zip = Join-Path $herramientas "apache-maven-$script:VersionMaven-bin.zip"
        $url = "https://archive.apache.org/dist/maven/maven-3/$script:VersionMaven/binaries/apache-maven-$script:VersionMaven-bin.zip"

        Write-Host ""
        Write-Host "Maven no esta instalado. Descargando Maven $script:VersionMaven..." -ForegroundColor Cyan
        Write-Host "  $url"
        try {
            Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
        } catch {
            throw "No se pudo descargar Maven. Comprueba la conexion o instala Maven manualmente. Detalle: $($_.Exception.Message)"
        }

        Write-Host "Descomprimiendo en .tools..." -ForegroundColor Cyan
        Expand-Archive -Path $zip -DestinationPath $herramientas -Force
        Remove-Item -Path $zip -Force -ErrorAction SilentlyContinue
    }

    if (-not (Test-Path $mvn)) {
        throw "No se encontro Maven en $mvn. Borra la carpeta .tools y vuelve a intentarlo."
    }
    return $mvn
}

function Invoke-MavenEnProyecto {
    param(
        [Parameter(Mandatory = $true)][string]$Proyecto,
        [Parameter(ValueFromRemainingArguments = $true)][string[]]$Argumentos
    )

    $mvn = Get-MavenEjecutable
    $directorio = Join-Path $script:RaizTrabajo $Proyecto
    if (-not (Test-Path $directorio)) {
        throw "No existe la carpeta del proyecto: $directorio"
    }

    Write-Host ""
    Write-Host "==> $Proyecto" -ForegroundColor Green
    Write-Host "    $mvn $($Argumentos -join ' ')" -ForegroundColor DarkGray
    Write-Host ""

    Push-Location $directorio
    try {
        & $mvn @Argumentos
        if ($LASTEXITCODE -ne 0) {
            throw "Maven termino con codigo $LASTEXITCODE en $Proyecto."
        }
    } finally {
        Pop-Location
    }
}

Write-Host "Raiz del trabajo: $script:RaizTrabajo" -ForegroundColor DarkGray
