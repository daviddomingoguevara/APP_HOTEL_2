# TRABAJO 2 · Usabilidad y métricas

**Asignatura:** Desarrollo de Interfaces (DAM)  
**Curso:** 2026-2027  
**Ubicación:** `TRABAJO 2/`

## Directorio del proyecto

El proyecto se encuentra en la carpeta `TRABAJO 2/` con la siguiente estructura:

```
TRABAJO 2/
├── aplicacion-basica/       (interfaz JavaFX versión A)
├── aplicacion-mejorada/     (interfaz JavaFX versión B)
├── datos/                   (XML hotel, CSV de ejemplo, JSONL de pruebas)
├── evidencias/              (capturas, screenshots, registros)
├── memoria/                 (memoria, metodología, resultados, guion)
├── scripts/                 (PowerShell y CMD para compilación y ejecución)
├── servidor/                (servidor local de métricas en Java)
└── web/                     (app HTML5/CSS3/JS responsiva para GitHub Pages)
```

## Aplicaciones JavaFX

### Versión A (básica)
- Interfaz simple con menú lateral Inicio/Habitaciones/Reservas/Usuarios/Configuración
- Formulario de búsqueda sin validación previa ni ayuda al usuario avanzada
- Feedback en texto plano, sin iconos ni accesibilidad avanzada
- `MainController.java` registra búsquedas, errores, intentos de finalizar y tiempo

### Versión B (mejorada)
- Jerarquía visual, feedback inmediato con texto+e iconos (no solo color)
- Validación de campo, búsqueda con tecla Enter, sugerencias de números
- Accesibilidad: contraste, etiquetas, orden de tabulación, foco visible
- Deshabilita el botón de finalizar hasta que se encuentra la habitación 103
- `MainController.java` mismas reglas de éxito/errors que la versión A

Ambas aplicaciones:
- Mismo XML hotel data (habitación 103 = **Doble, ocupada**, planta 1, accesible sí)
- Mismo criterio de éxito: localizar la habitación 103 y pulsar **finalizar** después de consultar su estado
- Usan reloj monótono (`System.nanoTime`) para cronometraje
- Graban métricas idénticas: duración, búsquedas, errores, éxito, abandono, primer-error, recuperación, orden, ID anónimo
- Si el servidor no está disponible, guardan resultados en CSV local: `%USERPROFILE%\.trabajo2-usabilidad\resultados-locales.csv`

## Servidor local de métricas

Ejecutar desde PowerShell en la raíz `TRABAJO 2/`:

```powershell
.\scripts\iniciar-servidor.ps1
```

- Puerto por defecto: 8080
- Sirve la web bajo `web/` para evitar problemas CORS
- API en `http://localhost:8080/api/*`
- Almacena `datos/pruebas.jsonl` y `datos/participantes.jsonl`
- Si el servidor está caído, las apps siguen grabando en CSV local

Para otro puerto: `.\scripts\iniciar-servidor.ps1 -Puerto 8099`

## Ejecución de las aplicaciones

Desde PowerShell en `TRABAJO 2/`:

```powershell
# Primera vez: Maven descargará JavaFX automáticamente
.\scripts\maven-comun.ps1

# Compilar y testear ambos proyectos
.\scripts\compilar-todo.ps1

# Iniciar el servidor (déjalo abierto durante la prueba)
.\scripts\iniciar-servidor.ps1

# Ejecutar la versión A (básica) - primera de acuerdo al orden asignado
.\scripts\ejecutar-basica.ps1

# Ejecutar la versión B (mejorada) - segunda de acuerdo al orden asignado
.\scripts\ejecutar-mejorada.ps1
```

Los scripts auto-descargan Maven 3.9.9 a `.tools` si no está instalado. No se necesita instalación previa.

## Página web (GitHub Pages)

La carpeta `web/` es una app HTML5/CSS3/JS responsiva, compatible con GitHub Pages:

- `index.html`: página de inicio con descripción del experimento
- `instrucciones.html`: pasos de registro, tarea y participación anónima
- `resultados.html`: visualización de métricas agregadas, charts, CSV import/export, modo demo

### Configuración API

Por defecto la web usa `http://localhost:8080` (mismo origen cuando se ejecuta en localhost). Se puede configurar la dirección del servidor en la página de configuración (`?api-base=...`). El modo offline permite importar el CSV local generado por las apps.

### Modo demo

Los resultados incluyen un modo demo con datos ficticios (`EJEMPLO-*`), claramente marcados con advertencia "DATOS FICTICIOS". No se usan como resultados reales.

## Memoria y documentación

### `memoria/`

- **memoria.md**: Memoria completa en español del proyecto (introducción, estado actual, pendientes)
- **metodologia.md**: Metodología utilizada, diseño de la experiencia, distribución equilibrada de órdenes A-B y B-A
- **resultados.md**: Plantilla de resultados con fórmulas explicadas y advertencia de datos pendientes
- **guion-presentacion.md**: Guion de presentación de 10-12 slides para exposición en clase

### `presentacion.md` (fuera del proyecto)

Fichero en la raíz del workspace (fuera de `TRABAJO 2/`) como prompt de Gemini centrado en la explicación en clase, no en el backend.

## Evidencias

- Capturas de pantalla de ambas aplicaciones en `evidencias/capturas/`
- Registro de pruebas técnicas en `evidencias/registro-pruebas-tecnicas.csv`
- Screenshots con avisos "DATOS FICTICIOS" cuando corresponde

## Fórmulas de métricas (definidas en `web/js/metrics.js`)

- **Tiempo medio**: media de `duracionSeg` de las pruebas exitosas de cada versión
- **Tasa de éxito**: `(éxitos / total_pruebas) × 100` (abandonadas e incompletas ≠ éxito)
- **Errores**: suma de `errores` por prueba; `errores/participante`, `errores/probar`
- **Abandono**: `%` de pruebas con `abandonada = true`
- **Eficiencia**: `(tasaÉxito / 100) / mediaTiempo × 100`
- **Mejora relativa**: `(mediaTiempo_A - mediaTiempo_B) / mediaTiempo_A × 100` (puede ser negativa)
- **Mejora emparejada**: media de `(A_i - B_i) / A_i × 100` sobre participantes que hicieron ambas versiones
- **% sin errores**: `(éxitos_sin_errores / total_éxitos) × 100`

## Advertencias importantes

- **No hay resultados reales aún**: el proyecto está en fase de desarrollo y validación. No se deben citar resultados cuantitativos como definitivos.
- **Datos ficticios**: el CSV `datos/ejemplo-resultados.csv` y su copia en `web/assets/` usan IDs `EJEMPLO-*` y deben ir acompañados de advertencia "DATOS FICTICIOS".
- **Habitación 103 = ocupada**: per el XML suministrado, la habitación 103 está ocupada, lo cual contradice la nota "debe estar disponible". Se sigue el XML y se documenta la discrepancia.
- **Sin causalidad**: las mejoras de la interfaz B pueden mejorar tiempos, pero también influyen aprendizaje, familiaridad y variabilidad entre personas. No se puede afirmar causalidad con la muestra actual.
- **GitHub Pages**: al publicar `web/`, las llamadas a `http://localhost:8080` pueden ser bloqueadas por HTTPS. Documentarse el modo offline y CSV import.

## Comandos PowerShell resumen

```powershell
# Ver esta ayuda: cat TRABAJO 2/README.md
# Compilar y test: .\scripts\compilar-todo.ps1
# Iniciar servidor: .\scripts\iniciar-servidor.ps1
# Versión A: .\scripts\ejecutar-basica.ps1
# Versión B: .\scripts\ejecutar-mejorada.ps1
# Compilar Maven: .\scripts\maven-comun.ps1
```