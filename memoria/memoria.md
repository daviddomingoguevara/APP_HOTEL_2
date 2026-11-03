# Memoria del Trabajo 2 · Usabilidad y métricas

## 1. Introducción

El presente trabajo consiste en la creación y evaluación de dos interfaces JavaFX para la consulta de habitaciones en un hotel, la versión A (básica) y la versión B (mejorada), con el objetivo de comparar su usabilidad mediante métricas cuantitativas. Ambas aplicaciones consumen el mismo fichero de datos `hotel.xml` y realizan la misma tarea: "Encuentra la habitación 103 y comprueba si está disponible". El experimento está diseñado como un estudio A/B equilibrado, donde los participantes realizan la tarea en ambas interfaces en órdenes contrarios para minimizar el efecto de aprendizaje.

## 2. Estado actual del proyecto

El proyecto se encuentra en fase de desarrollo yvalidación preliminar. Se han completado los componentes fundamentales:

- Estructura de directorios `TRABAJO 2/` con separación de `apps`, `web`, `data`, `memoria`, `evidencias`.
- Implementación del modelo de datos (`Habitacion`, `Reserva`, `Cliente`) y el `HotelDAO` a partir del XML suministrado.
- Capa de métricas compartida (`RegistroPrueba`, `SesionPrueba`, `EventoPrueba`, `Json`, `ApiCliente`, `EnvioMetricas`) que registra duración via `System.nanoTime`, número de búsquedas, errores, éxito, abandono, primer-error time, recovery time, orden de participación y ID anónimo.
- Aplicación básica (versión A) con interfaz funcional, capacidad de búsqueda, validación mínima y registro de métricas.
- Aplicación mejorada (versión B) con corrección de error crítico (añadido de `fx:id="seccionInicio"`) que habilita el inicio correcto de la aplicación.
- Servidor local de métricas (`ServidorMetricas.java`) usando `com.sun.net.httpserver`, que sirve tanto las APIs como los recursos estáticos de la web.
- Página web responsiva (`web/`) en HTML5/CSS3/JS para registro de participantes, visualización de resultados, import/export de CSV y modo demo con datos claramente marcados como ficticios.

## 3. Arquitectura y diseño

### 3.1 Aplicaciones JavaFX

Ambas aplicaciones comparten la misma estructura de controladores y métricas, diferenciándose principalmente en el FXML y el CSS:

- **Modelo**: `HotelDAO` singleton que carga `datos/hotel.xml` y proporciona métodos para buscar habitaciones por número y contar por estado.
- **Controlador (`MainController`)**: gestiona la navegación entre secciones (Inicio, Habitaciones, Reservas, Usuarios, Configuración), el flujo de búsqueda, el botón de finalizar y el cronómetro.
- **Métricas (`SesionPrueba`)**: usa `System.nanoTime` como reloj monótono. Cada sesión registra eventos (`busqueda-correcta`, `error-busqueda`, `error-intento-finalizar`, `prueba-completada`, `prueba-abandonada`). El criterio de éxito es localizar la habitación 103 y pulsar finalizar después de consultar su estado.

### 3.2 Web (GitHub Pages)

La página web sirve como interfaz de registro y visualización de resultados agregados:

- Registro de participante con código anónimo (2-20 caracteres, letras/números/guión/guión bajo).
- Asignación equilibrada de orden de pruebas (A-B o B-A) por el servidor.
- API REST sobre el servidor Java local, con fallback a `localStorage` y CSV import si el servidor no está disponible.
- Gráficos a partir de los datos reales recibidos; conclusiones generadas condicionalmente (sin afirmar predeterminadamente "B es mejor").
- CSV import/export con fórmulas de cálculo idénticas a las del backend.
- Modo demo con datos `EJEMPLO-*` y avisos "DATOS FICTICIOS".

### 3.3 Servidor de métricas

- `ServidorMetricas.java` extiende `com.sun.net.httpserver.HttpServer`.
- Endpoints: `GET /api/salud`, `GET/POST /api/participantes`, `GET/POST /api/pruebas`.
- CORS habilitado para permitir peticiones desde `localhost`.
- Participante orders alternan A-B/B-A de forma automática.
- Datos persistidos en `datos/pruebas.jsonl` y `datos/participantes.jsonl`.

## 4. Metricas y fórmulas

Las siguientes métricas se calculan a partir de los datos recopilados:

| Métrica | Fórmula | Descripción |
|---------|---------|-------------|
| Media tiempo (éxitos) | `media(duracionSeg de éxitos)` | Segundos promedio hasta completar la tarea |
| Tasa de éxito | `(éxitos / total) × 100` | Pruebas completadas correctamente |
| Errores (total) | `suma(errores)` | Búsquedas incorrectas + intentos de finalizar sin hallar 103 |
| Errores/media | `erroresTotales / total` | Promedio de errores por prueba |
| Abandono | `(abandonadas / total) × 100` | Pruebas cerradas sin completar |
| Eficiencia | `(tasaÉxito / 100) / mediaTiempo × 100` | Relación entre éxito y tiempo |
| Mejora relativa | `(mediaTiempo_A - mediaTiempo_B) / mediaTiempo_A × 100` | Porcentaje de mejora de B sobre A (negativo significa peor) |
| Mejora emparejada | `media((A_i - B_i) / A_i × 100)` | Sobre participantes que hicieron ambas versiones; reduce variabilidad individual |
| % sin errores | `(éxitos_sin_errores / total_éxitos) × 100` | De las exitosas, cuántas tuvieron 0 errores |

**Notas importantes:**
- Las pruebas abandonadas o sin finalizar no cuentan como éxito.
- La habitación 103 está definida como **ocupada** en el XML suministrado; el éxito significa localizarla y comprobar su estado, no que esté disponible.
- La mejora relativa puede ser negativa si la versión B tarda más de media; esto se muestra sin ocultar.
- Las conclusiones deben indicar que las diferencias pueden deberse al aprendizaje, variabilidad individual u otros factores, no solo a la interfaz.

## 5. Resultados (plantilla pendiente de datos reales)

> **Nota:** Esta sección debe actualizarse una vez recopilados los datos del experimento con participantes reales. Por el momento contiene la estructura y fórmulas, pero los valores están pendientes.

- **Participantes**: [Número total, distribuidos en órdenes A-B y B-A]
- **Pruebas por versión**: A = [N], B = [N]
- **Tiempo medio A**: [X.XX s], B: [X.XX s]
- **Mejora relativa**: [X%]` (puede ser negativa)
- **Tasa de éxito A**: [X%], B: [X%]
- **Diferencia de éxito**: [X] puntos porcentuales
- **Errores/media A**: [X.XX], B: [X.XX]
- **Abandono A**: [X%], B: [X%]
- **Mejora emparejada**: [X%]` (sobre [N] participantes con ambas versiones)
- **Principales conclusiones**: [texto]

## 6. Guion de presentación (10-12 slides)

> Ver `memoria/guion-presentacion.md` para el guion detallado.

## 7. Pendientes y limitaciones

- No se ha realizado un experimento con participantes reales; los datos de validación usan una combinación de pruebas automáticas y CSV de ejemplo ficticio.
- La habitación 103 está **ocupada** según el XML, lo que contradice la nota "debe estar disponible"; se sigue el dato del XML y se documenta.
- El número de participantes es pequeño; los resultados no permiten generalizar más allá del aula.
- Las conclusiones deben ser cautelosas y no afirmar causalidad directa entre la interfaz y los resultados.
- El servidor local es necesario para compartir resultados entre todos los participantes; el modo offline funciona pero los datos no se comparten automáticamente.