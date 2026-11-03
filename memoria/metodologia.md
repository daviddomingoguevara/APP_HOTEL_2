# Metodología del Trabajo 2 · Usabilidad y métricas

## 1. Diseño del experimento A/B

El experimento sigue un diseño A/B equilibrado para comparar dos interfaces que realizan la misma tarea con diferentes niveles de usabilidad. El objetivo es determinar si las mejoras de la interfaz B se traducen en diferencias medibles de tiempo, errores y éxito respecto a la versión A básica.

### 1.1 Distribución equilibrada de órdenes

Para evitar que el efecto de aprendizaje favorezca siempre a la misma versión, el orden de las pruebas se asigna de forma equilibrada:

- **Grupo A-B**: Primero realiza la versión A, luego la versión B.
- **Grupo B-A**: Primero realiza la versión B, luego la versión A.
- El número de participantes en cada grupo debe ser el más equilibrado posible (diferencia mÁximo 1).

El orden se asigna cuando el participante se registra en la web, consultando el servidor o usando modo local con CSV.

### 1.2 Tarea única

La tarea para todos los participantes es idéntica:

> **"Encuentra la habitación 103 y comprueba si está disponible."**

Los únicos datos que varían son:
- El código anónimo elegido por el participante.
- El orden de las versiones (A-B o B-A), asignado equilibradamente.
- Los tiempos, búsquedas y errores que la propia aplicación registra.

### 1.3 Criterio de éxito

Se considera que la tarea se completa correctamente cuando la persona:
1. Localiza la habitación 103 usando el buscador.
2. Consulta el estado de la habitación (ocupada/disponible/mantenimiento).
3. Pulsar el botón **finalizar** después de haber comprobado el estado.

Las pruebas que se cierran sin finalizar, las abandonadas o las que no localizan la habitación **no** cuentan como éxito.

## 2. Aplicaciones JavaFX

### 2.1 Versión A (básica)

- Interfaz inicial sin ayudas específicas de usabilidad.
- Menú lateral con secciones Inicio/Habitaciones/Reservas/Usuarios/Configuración.
- Formulario de búsqueda sencillo, sin validación previa.
- Feedback en texto plano al buscar o al intentar finalizar sin haber encontrado la habitación.
- El botón de finalizar está activo desde el inicio (aunque el criterio de éxito no se cumpla).

### 2.2 Versión B (mejorada)

- Aplica mejoras de jerarquía visual, feedback inmediato, accesibilidad y prevención de errores.
- Buscador destacado con etiqueta clara y ayuda contextual.
- Validación del campo de entrada (solo 3 cifras, Enter para buscar).
- Estados de la habitación diferenciados con texto e iconos, sin depender solo del color.
- Foco visible en el campo de búsqueda, orden de tabulación coherente.
- El botón de finalizar se deshabilita hasta que se encuentra la habitación 103.
- Sugerencias de números basadas en búsquedas previas.

## 3. Registro de métricas

### 3.1 Capa compartida (Java)

Ambas aplicaciones usan la misma clase `SesionPrueba` con las siguientes características:

- **Reloj monótono**: `System.nanoTime` para medir duraciones; un cambio de hora del sistema no altera el resultado.
- **Eventos registrados**: cada búsqueda, error, intento de finalizar y el evento de completado/abandono van a una lista serializable a JSON.
- **Criterios de transición**:
  - Si se busca la habitación 103 → `encontrada = true`, se guarda el estado (`ocupada`, `disponible`, `mantenimiento`).
  - Si se pulsa finalizar sin haber encontrado 103 → error-contabilizado, la prueba no se considera completada.
  - Si el usuario cierra la ventana → registrada como `abandonada`.

### 3.2 Variables por prueba

Cada prueba registra:
- ID anónimo (UUID generado o código proporcionado).
- Versión (A o B).
- Orden de participación (A-B, B-A o no-disponible si el servidor no responde).
- Duración total (ms y s) desde el inicio hasta el fin o abandono.
- Número de búsquedas realizadas.
- Número de errores (búsquedas incorrectas + intentos de finalizar sin hallar 103).
- Si fue éxito, completada o abandonada.
- Tiempo hasta el primer error (ms).
- Tiempo de recuperación después del primer error (ms).
- Resultado final (`encontrada-ocupada`, `encontrada-disponible`, `abandonada`, `sin-habitacion-encontrada`).

### 3.3 Servidor y almacenamiento

- El servidor guarda cada prueba en `datos/pruebas.jsonl` (un JSON por línea).
- También mantiene `datos/participantes.jsonl` con el mapeo participante-orden.
- Si el servidor no está disponible, la aplicación guarda un CSV local en `%USERPROFILE%\.trabajo2-usabilidad\resultados-locales.csv`.
- El CSV local tiene las mismas columnas y se puede importar posteriormente desde la web.

## 4. Página web (HTML5/CSS3/JS)

### 4.1 Flujo de participación

1. El participante entra a `index.html` y pulsa "Comenzar el experimento".
2. Se abre `instrucciones.html` donde:
   - Elige un código anónimo (2-20 caracteres, sin datos personales).
   - Se le asigna un orden equilibrado (A-B o B-A).
   - Se le indica si el servidor está encendido o si trabaja en modo offline/CSV local.
3. El participante abre las dos aplicaciones JavaFX según su orden asignado, escribe su código cuando cada una lo solicite.
4. Realiza la tarea en cada versión, finaliza y cierra la aplicación.
5. Pulsa "Comprobar mi progreso" en la web para ver sus datos y los agregados.

### 4.2 Visualización de resultados

La página `resultados.html` muestra:

- KPIs: tiempo medio, tasa de éxito, errores, abandono.
- Charts a partir de los datos reales recibidos.
- Mejora relativa y diferencias entre versiones.
- CSV import/export para descargar o subir datos.
- Modo demo con datos `EJEMPLO-*` y avisos "DATOS FICTICIOS".
- Filtros por versión y orden para segmentar los datos.

### 4.3 Fórmulas en JavaScript (web/js/metrics.js)

Todas las fórmulas de cálculo están en `web/js/metrics.js` y son idénticas al backend de Java:

- `esExito(registro)`: `registro.exito && registro.completado && !registro.abandonada`
- `media(valores)`: `reduce(sum) / length`
- `mediana(valores)`: valor central ordenado.
- `estadisticasVersion(registros)`: calcula todos los KPIs descriptos en laREADME.
- `calcular(pruebas)`: divide por versiones, calcula parejas, mejora emparejada, diferencias.
- `generarConclusiones(datos, opciones)`: genera texto de conclusiones condicionado a que haya datos reales y no demo.

## 5. Consideraciones éticas y de privacidad

- No se recogen nombres, correos, direcciones ni ningún dato personal.
- Solo se guarda un código anónimo elegido por el participante (2-20 caracteres).
- Los datos se usan exclusivamente para el trabajo académico de la asignatura Desarrollo de Interfaces.
- Los participantes pueden repetir la consulta de resultados las veces que deseen.
- Si el participante comete un error (cierra la ventana por accidente), se registra como abandono; se le aconseja usar un código nuevo en futuras ocasiones.

## 6. Limitaciones metodológicas

- El número de participantes está limitado por el contexto de aula; los resultados no son generalizables más allá.
- La tarea única y el entorno controlado pueden no reflejar situaciones de uso real.
- La habitación 103 está ocupada según el XML; esto puede afectar la motivación de los participantes y sesgar los tiempos.
- El efecto de aprendizaje no puede separarse completamente de la mejora de la interfaz, aunque el diseño A/B mitiga parcialmente este problema.
- No se mide satisfacción subjetiva ni cualitativa; solo métricas objetivas de tiempo y errores.
- Las conclusiones deben reflejar estas limitaciones y no afirmar causalidad directa.