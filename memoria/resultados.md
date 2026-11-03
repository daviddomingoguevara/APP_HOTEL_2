# Resultados del Trabajo 2 · Usabilidad y métricas

> **Nota importante:** Los valores que aparecen a continuación son una **plantilla** y datos de ejemplo con datos ficticios (`EJEMPLO-*`). No representan resultados reales de participación de personas. Cuando se disponga de datos reales de participantes, esta sección debe actualizarse completamente.

## 1. Estructura de los datos

Las métricas se recogen en formato JSONL (`datos/pruebas.jsonl`) y CSV (`datos/ejemplo-resultados.csv` y `web/assets/ejemplo-resultados.csv`). Cada registro contiene:

- `participante`: código anónimo (ej. `P01`, `anonimo`).
- `version`: `A` o `B`.
- `orden`: `A-B`, `B-A` o `no-disponible`.
- `duracionSeg`: tiempo en segundos (número redondeado a 2 decimales).
- `busquedas`: número de intentos de búsqueda.
- `errores`: número de errores (búsquedas incorrectas + intentos de finalizar sin hallar 103).
- `exito`: booleano, `true` solo si la prueba fue éxito (localizó 103 y finalizó).
- `completada`: booleano, `true` si se pulsó finalizar después de encontrar 103.
- `abandonada`: booleano, `true` si se cerró la ventana antes de finalizar.
- `resultado`: cadena descriptiva (`encontrada-ocupada`, `encontrada-disponible`, `abandonada`, `sin-habitacion-encontrada`).

## 2. Estadísticas globales (plantilla)

| Métrica | Descripción |
|---------|-------------|
| Total de pruebas | Número total de registros válidos |
| Participantes | Número de códigos anónimos únicos |
| Participantes con ambas versiones | `conAmbas`: participantes que completaron A y B |
| Éxitos (totales) | Pruebas con `exito = true` y `!abandonada` |
| Tasa de éxito | `(éxitos / total) × 100` |
| Abandonos | Pruebas con `abandonada = true` |
| Tasa de abandono | `(abandonos / total) × 100` |
| Errores totales | Suma de todos los `errores` |
| Errores/media | `erroresTotales / total` |
| Media tiempo | `media(duracionSeg de los éxitos)` |
| Mediana tiempo | `mediana(duracionSeg de los éxitos)` |
| Min tiempo | mínimo de los éxitos |
| Max tiempo | máximo de los éxitos |
| Eficiencia | `(tasaÉxito / 100) / mediaTiempo × 100` |

## 3. Análisis por versión (A y B)

| Métrica | Versión A | Versión B | Diferencia |
|---------|-----------|-----------|------------|
| Total pruebas | [N] | [N] | — |
| Éxitos | [N] | [N] | — |
| Tasa de éxito | [X]% | [Y]% | [Z] pp `[B>A` o `A>B]` |
| Media tiempo | [X.XX] s | [Y.YY] s | `[W.W]%` `mejor` `peor` |
| Mediana tiempo | [X.XX] s | [Y.YY] s | — |
| Errores/media | [X.XX] | [Y.YY] | [Z.ZZ] menos `más` por probar |
| Errores 0% | [X]% | [Y]% | — |
| Abandono | [X]% | [Y]% | — |

### 3.1 Mejora relativa

- **Mejoras tiempo media**: `(mediaTiempo_A - mediaTiempo_B) / mediaTiempo_A × 100`
  - Si positivo: B tarda menos de media.
  - Si negativo: B tarda más; el signo se muestra sin ocultar.
- **Mejoras emparejadas**:media de `(A_i - B_i) / A_i × 100` sobre participantes que hicieron ambas versiones.
- **Diferencia de éxito**: `tasaÉxito_B - tasaÉxito_A` (en puntos porcentuales).

## 4. Análisis por orden de pruebas

| Orden | Pruebas | Participantes | Media tiempo A | Media tiempo B | Mejora B sobre A |
|-------|---------|---------------|----------------|----------------|------------------|
| A-B | [N] | [N] | [X.XX] s | [Y.YY] s | [Z.Z]% |
| B-A | [N] | [N] | [X.XX] s | [Y.YY] s | [Z.Z]% |

> **Observación:** Si el orden influye, el tiempo de B variará dependiendo de si se hizo después de A o primero. Conviene tenerlo en cuenta al interpretar la mejora.

## 5. Conclusiones (generadas condicionalmente)

Las conclusiones se generan a partir de los datos reales, sin afirmar predeterminadamente que "B es mejor". Estructura de conclusiones esperada:

- Si hay mejora de tiempo positiva: "En esta muestra, la versión B tardó X% menos de media que la versión A (Y s frente a Z s)."
- Si hay mejora de tiempo negativa: "En esta muestra, la versión B tardó X% más que la versión A. El resultado negativo se muestra sin ocultarlo."
- Si no hay datos suficientes: "Faltan pruebas de una de las dos versiones para comparar el tiempo medio."
- Si hay diferencia de éxito: "La tasa de éxito de B fue de X puntos porcentuales más alta/inferior a la de A."
- Si hay diferencia de errores: "La media de errores por prueba fue de X en A y Y en B: B registró Z errores menos/más por prueba."
- Entre participantes con ambas versiones: "Entre los N participantes que completaron ambas versiones, la mejora media fue de X%."
- Sobre orden de pruebas: "El orden de las pruebas puede influir: el tiempo medio de B fue X s cuando se hizo después de A y Y s cuando se hizo primero."
- Advertencia siempre: "Las metricas no miden satisfaccion ni accesibilidad subjetiva. Para valorar si una interfaz es mejor hay que considerar los errores, la tasa de exito, la accesibilidad y la opinion de las personas."

## 6. Datos de ejemplo ficticios (EJEMPLO)

El fichero `datos/ejemplo-resultados.csv` contiene 7 participantes ficticios con IDs `EJEMPLO-1` a `EJEMPLO-7` y 14 tests (7 por versión). Métricas de ejemplo:

| Versión | Media tiempo | Tasa éxito | Errores/media | % sin errores |
|---------|-------------|------------|---------------|---------------|
| A | 41.58 s | 85.7% | 1.14 | — |
| B | 25.21 s | 100% | 0.29 | — |

**Mejora relativa**: `(41.58 - 25.21) / 41.58 × 100 ≈ 39.4%` (la B tarda un 39.4% menos de media).

> **Advertencia:** Estos datos son ficticios y solo sirven para demostrar el funcionamiento de las gráficas y conclusiones. No deben citarse como resultados reales de experimento alguno.

## 7. Pendientes

- Recolectar datos reales con participantes en el aula.
- Actualizar tablas y conclusiones con datos reales.
- Validar que la mejora sea estadísticamente significativa (o indicar que no lo suficiente con la muestra).
- Documentar cualquier incidente o anomalía durante la recolección.