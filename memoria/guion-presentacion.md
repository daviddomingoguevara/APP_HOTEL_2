# Guion de presentación (10-12 slides)

## Slide 1: Portada
- Título: "Experimento de usabilidad: interfaz básica vs. mejorada"
- Asignatura: Desarrollo de Interfaces (DAM)
- Curso/Año: 2026-2027
- Autor: [Nombre]

## Slide 2: Contexto y problema
- Las interfaces de gestión hotelera deben ser usables y accesibles.
- Pequeñas mejoras de usabilidad pueden tener grandes impactos en eficiencia.
- Pregunta de investigación: ¿una interfaz mejor ayuda a los usuarios a completar tareas más rápido y con menos errores?

## Slide 3: Objetivo del experimento
- Comparar dos versiones JavaFX de la misma aplicación.
- Versión A: interfaz básica, sin ayudas avanzadas.
- Versión B: interfaz con mejoras de jerarquía, feedback, accesibilidad y prevención de errores.
- Misma tarea, mismos datos, órdenes A/B equilibradas.

## Slide 4: La tarea
- "Encuentra la habitación 103 y comprueba si está disponible."
- La habitación 103 es **Doble**, **ocupada**, planta 1, accesible sí.
- Criterio de éxito: localizar 103 y pulsar **finalizar** después de consultar su estado.
- Errores: cerrar sin finalizar, buscar 103 sin finalizar, no localizar la habitación.

## Slide 5: Versión A (básica)
- Interfaz simple, menú lateral, formulario de búsqueda.
- Sin validación previa, feedback en texto plano.
- El botón finalizar está activo desde el inicio.
- No hay ayuda contextual ni sugerencias.
- Accesibilidad básica; retroalimentación solo visual.

## Slide 6: Versión B (mejorada)
- Jerarquía visual clara, buscador destacado.
- Validación de campo (solo 3 cifras, Enter para buscar).
- Feedback con texto e iconos (no solo color).
- Foco visible, orden de tabulación coherente.
- Botón finalizar deshabilitado hasta encontrar la habitación 103.
- Sugerencias de números basadas en historial.
- Estados diferenciados con texto e iconos.

## Slide 7: Métricas que se recogen
- Tiempo de duración (reloj monótono `nanoTime`).
- Número de búsquedas.
- Errores (búsquedas incorrectas + intentos de finalizar sin hallar 103).
- Éxito (localizó 103 y finalizó).
- Abandono (cerró la ventana antes de terminar).
- Primer error time y tiempo de recuperación.
- Orden de participación (A-B o B-A) e ID anónimo.

## Slide 8: Diseño del experimento A/B
- Dos grupos: A-B y B-A.
- Orden asignado en el registro web, balanceado.
- Misma persona hace ambas versiones, en distinto orden.
- Evita que el aprendizaje favorezca siempre a una misma interfaz.

## Slide 9: Resultados esperados (con datos reales)
- Media de tiempo por versión.
- Tasa de éxito (A vs B).
- Errores por versión.
- Abandono por versión.
- Mejora relativa `(A−B)/A×100` (puede ser negativa).
- Eficiencia index explicada.
- Conclusiones cautelosas, sin afirmar causalidad directa.

## Slide 10: Limitaciones y precauciones
- Muestra pequeña (contexto de aula).
- La habitación 103 está ocupada según el XML; puede afectar motivación.
- No se mide satisfacción subjetiva.
- Las diferencias pueden deberse al aprendizaje, familiaridad o variabilidad individual.
- No se puede afirmar causalidad con los datos actuales.

## Slide 11: Demo y funcionamiento
- Mostrar la web de registro (`instrucciones.html`).
- Mostrar la página de resultados (`resultados.html`) con modo demo marcado como "DATOS FICTICIOS".
- Mostrar cómo se importaría un CSV local.
- Comentar que el servidor local (`iniciar-servidor.ps1`) comparte datos entre todos.

## Slide 12: Conclusiones y trabajo futuro
- El proyecto está en fase de validación preliminar.
- Los scripts y estructura están completados; los datos reales pendientes.
- Futuro: más participantes, análisis estadístico deeper, expansión de métricas (satisfacción subjetiva, heatmaps, etc.).
- Agradecimientos y preguntas.

En un momento de la presentacion se debe poner esto en la pantalla como explicación a los alumnos:

# 1. Poner el servidor de métricas (solo una persona o el profesor)
.\scripts\iniciar-servidor.ps1

# 2. Registrarse en la web
#    - Abrir web/index.html en el navegador
#    - Elegir código anónimo, aceptar participar
#    - Anotar el código y el orden (A-B o B-A) que les toca

# 3. Abrir las dos aplicaciones JavaFX según su orden
#    - Si les toca empezar por A:
.\scripts\ejecutar-basica.ps1
#    - Cuando pida código, escribir el suyo
#    - Repetir con la versión B usando el mismo código

# 4. Consultar resultados
#    - Volver a web/resultados.html para ver sus datos y los agregados