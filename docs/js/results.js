/* ============================================================
   Pagina de resultados: carga de datos, metricas, graficos,
   tabla individual, conclusiones, importacion y exportacion.
   ============================================================ */
(function () {
    "use strict";

    const estado = {
        pruebas: [],
        datos: null,
        modo: "vacio",
        demo: false,
        filtro: null,
        origen: ""
    };

    function $(selector) {
        return document.querySelector(selector);
    }

    function texto(id, valor) {
        const elemento = document.getElementById(id);
        if (elemento) {
            elemento.textContent = valor;
        }
    }

    function mostrar(id, visible) {
        const elemento = document.getElementById(id);
        if (elemento) {
            elemento.hidden = !visible;
        }
    }

    function formatearFecha(iso) {
        if (!iso) return "—";
        const fecha = new Date(iso);
        if (Number.isNaN(fecha.getTime())) {
            return String(iso);
        }
        return fecha.toLocaleString("es-ES", {
            day: "2-digit", month: "2-digit", year: "numeric",
            hour: "2-digit", minute: "2-digit"
        });
    }

    function formatearFechaCorta(iso) {
        if (!iso) return "—";
        const fecha = new Date(iso);
        if (Number.isNaN(fecha.getTime())) {
            return String(iso);
        }
        return fecha.toLocaleDateString("es-ES", { day: "2-digit", month: "2-digit", year: "numeric" });
    }

    function fechaArchivo() {
        const ahora = new Date();
        const dos = (n) => String(n).padStart(2, "0");
        return ahora.getFullYear() + dos(ahora.getMonth() + 1) + dos(ahora.getDate()) + "-" +
            dos(ahora.getHours()) + dos(ahora.getMinutes());
    }

    function descargar(nombre, contenido, tipo) {
        const blob = new Blob([contenido], { type: tipo });
        const enlace = document.createElement("a");
        enlace.href = URL.createObjectURL(blob);
        enlace.download = nombre;
        document.body.appendChild(enlace);
        enlace.click();
        enlace.remove();
        window.setTimeout(() => URL.revokeObjectURL(enlace.href), 1500);
    }

    /* ---------- Carga ---------- */
    async function cargar() {
        mostrar("cargando", true);
        mostrar("sin-datos", false);
        mostrar("panel-resultados", false);
        const parametros = new URLSearchParams(window.location.search);
        estado.filtro = parametros.get("participante");

        if (parametros.get("demo") === "1") {
            await cargarDemo();
            return;
        }

        try {
            estado.pruebas = await window.Trabajo2Api.obtenerPruebas();
            estado.modo = "servidor";
            estado.demo = false;
            estado.origen = "Servidor local";
        } catch (e) {
            estado.pruebas = window.Trabajo2Api.pruebasImportadas();
            estado.modo = "local";
            estado.demo = false;
            estado.origen = "Datos importados en este navegador";
        }
        finalizarCarga();
    }

    async function cargarDemo() {
        try {
            const respuesta = await fetch("assets/ejemplo-resultados.csv", { cache: "no-store" });
            if (!respuesta.ok) {
                throw new Error("No se pudo cargar el ejemplo");
            }
            const csv = await respuesta.text();
            const filas = window.Trabajo2Metrics.parseCsv(csv);
            estado.pruebas = filas;
            estado.modo = "demo";
            estado.demo = true;
            estado.origen = "Datos ficticios de ejemplo";
        } catch (e) {
            estado.pruebas = [];
            estado.modo = "demo";
            estado.demo = true;
            estado.origen = "Datos ficticios de ejemplo";
        }
        estado.filtro = null;
        finalizarCarga();
    }

    function finalizarCarga() {
        mostrar("cargando", false);
        estado.datos = window.Trabajo2Metrics.calcular(estado.pruebas);
        if (!estado.datos || estado.datos.global.totalPruebas === 0) {
            mostrar("sin-datos", true);
            mostrar("panel-resultados", false);
            actualizarBannerModo();
            actualizarToolbar();
            return;
        }
        mostrar("sin-datos", false);
        mostrar("panel-resultados", true);
        renderizar();
        actualizarToolbar();
    }

    function actualizarToolbar() {
        const hayDatos = Boolean(estado.datos && estado.datos.global.totalPruebas > 0);
        ["btn-exportar", "btn-exportar-json", "btn-imprimir"].forEach((id) => {
            const boton = document.getElementById(id);
            if (boton) {
                boton.disabled = !hayDatos;
            }
        });
        mostrar("btn-salir-demo", estado.demo);
        const limpiar = $("#btn-limpiar-importadas");
        if (limpiar) {
            limpiar.hidden = estado.modo !== "local";
        }
    }

    function actualizarBannerModo() {
        const banner = $("#banner-modo");
        const badge = $("#badge-modo");
        const detalle = $("#modo-detalle");
        if (!banner) return;
        banner.className = "alert";
        if (estado.demo) {
            banner.classList.add("alert-demo");
            badge.className = "badge badge-warn";
            badge.textContent = "DEMO";
            detalle.textContent = "Estas viendo un conjunto de datos FICTICIO incluido para demostrar la pagina. " +
                "No son resultados de participantes reales y no deben usarse en la memoria.";
        } else if (estado.modo === "servidor") {
            banner.classList.add("alert-success");
            badge.className = "badge badge-success";
            badge.textContent = "Servidor";
            detalle.textContent = "Datos reales obtenidos del servidor local. " +
                (estado.datos ? estado.datos.global.totalPruebas + " prueba(s) registrada(s)." : "");
        } else if (estado.modo === "local") {
            banner.classList.add("alert-warning");
            badge.className = "badge badge-warn";
            badge.textContent = "Importados";
            detalle.textContent = "El servidor local no responde. Se muestran las pruebas importadas en este navegador. " +
                "Para compartirlas, exporta el CSV o vuelve a importarlas con el servidor encendido.";
        } else {
            banner.classList.add("alert-info");
            badge.className = "badge badge-neutral";
            badge.textContent = "Sin datos";
            detalle.textContent = "Todavia no hay resultados. Sigue las instrucciones para realizar las pruebas.";
        }
    }

    /* ---------- Render ---------- */
    function renderizar() {
        const d = estado.datos;
        actualizarBannerModo();
        mostrar("alerta-demo", estado.demo);
        mostrar("banner-filtro", Boolean(estado.filtro));

        texto("kpi-participantes", String(d.global.participantes));
        texto("kpi-pruebas", String(d.global.totalPruebas));
        texto("kpi-tiempo-a", d.A.mediaTiempo === null ? "—" : d.A.mediaTiempo.toFixed(2) + " s");
        texto("kpi-tiempo-b", d.B.mediaTiempo === null ? "—" : d.B.mediaTiempo.toFixed(2) + " s");
        texto("kpi-mejora", d.mejoraTiempo === null ? "—" :
            (d.mejoraTiempo > 0 ? "−" : "+") + Math.abs(d.mejoraTiempo).toFixed(1) + " %");
        texto("kpi-exito-a", window.Trabajo2Metrics.formatearPct(d.A.tasaExito));
        texto("kpi-exito-b", window.Trabajo2Metrics.formatearPct(d.B.tasaExito));
        texto("kpi-errores-a", d.A.erroresPorPrueba === null ? "—" : d.A.erroresPorPrueba.toFixed(2));
        texto("kpi-errores-b", d.B.erroresPorPrueba === null ? "—" : d.B.erroresPorPrueba.toFixed(2));

        const muestra = $("#resumen-muestra");
        if (muestra) {
            const partes = [];
            if (d.global.participantes < 10) {
                partes.push("Muestra pequeña (" + d.global.participantes +
                    " participantes): los resultados son descriptivos y no permiten generalizar.");
            }
            if (d.global.participantesConAmbas < d.global.participantes) {
                partes.push(d.global.participantesConAmbas + " participante(s) completaron las dos versiones.");
            }
            if (d.invalidas > 0) {
                partes.push(d.invalidas + " registro(s) no se pudieron interpretar y se excluyeron.");
            }
            muestra.textContent = partes.join(" ");
            muestra.hidden = partes.length === 0;
        }

        const fechas = d.global.primeraFecha || d.global.ultimaFecha
            ? "Pruebas registradas entre " + formatearFechaCorta(d.global.primeraFecha) +
              " y " + formatearFechaCorta(d.global.ultimaFecha) + "."
            : "";
        texto("resumen-fechas", fechas);
        texto("resumen-modo", estado.origen);

        renderizarGraficos(d);
        renderizarComparativa(d);
        renderizarOrdenes(d);
        renderizarConclusiones(d);
        renderizarTablaIndividual(d);
    }

    function crearFilaGrafico(etiqueta, valor, maximo, clase, textoValor) {
        const fila = document.createElement("div");
        fila.className = "chart-row";
        const label = document.createElement("span");
        label.className = "chart-label";
        label.textContent = etiqueta;
        const track = document.createElement("div");
        track.className = "chart-track";
        const barra = document.createElement("div");
        barra.className = "chart-bar " + clase;
        const porcentaje = valor === null || valor === undefined || maximo <= 0
            ? 0
            : Math.max(valor > 0 ? 2 : 0, Math.min(100, (valor / maximo) * 100));
        barra.style.width = porcentaje + "%";
        track.appendChild(barra);
        const value = document.createElement("span");
        value.className = "chart-value";
        value.textContent = textoValor;
        fila.append(label, track, value);
        return fila;
    }

    function pintarGrafico(contenedorId, filas, etiquetaAccesible) {
        const contenedor = document.getElementById(contenedorId);
        if (!contenedor) return;
        contenedor.replaceChildren();
        const valores = filas.map((f) => f.valor).filter((v) => v !== null && v !== undefined);
        if (!valores.length) {
            const vacio = document.createElement("p");
            vacio.className = "chart-empty";
            vacio.textContent = "Sin datos suficientes para este grafico.";
            contenedor.appendChild(vacio);
            return;
        }
        const maximo = Math.max(...valores, 0.0001);
        for (const fila of filas) {
            contenedor.appendChild(crearFilaGrafico(
                fila.etiqueta, fila.valor, maximo, fila.clase, fila.texto
            ));
        }
        contenedor.setAttribute("role", "img");
        contenedor.setAttribute("aria-label", etiquetaAccesible);
    }

    function renderizarGraficos(d) {
        pintarGrafico("chart-tiempo", [
            {
                etiqueta: "A",
                valor: d.A.mediaTiempo,
                clase: "bar-a",
                texto: window.Trabajo2Metrics.formatearSegundos(d.A.mediaTiempo)
            },
            {
                etiqueta: "B",
                valor: d.B.mediaTiempo,
                clase: "bar-b",
                texto: window.Trabajo2Metrics.formatearSegundos(d.B.mediaTiempo)
            }
        ], "Tiempo medio de busqueda: version A " + window.Trabajo2Metrics.formatearSegundos(d.A.mediaTiempo) +
            ", version B " + window.Trabajo2Metrics.formatearSegundos(d.B.mediaTiempo));

        pintarGrafico("chart-exito", [
            {
                etiqueta: "A",
                valor: d.A.tasaExito,
                clase: "bar-a",
                texto: window.Trabajo2Metrics.formatearPct(d.A.tasaExito)
            },
            {
                etiqueta: "B",
                valor: d.B.tasaExito,
                clase: "bar-b",
                texto: window.Trabajo2Metrics.formatearPct(d.B.tasaExito)
            }
        ], "Tasa de exito: version A " + window.Trabajo2Metrics.formatearPct(d.A.tasaExito) +
            ", version B " + window.Trabajo2Metrics.formatearPct(d.B.tasaExito));

        const maxErrores = Math.max(
            d.A.erroresPorPrueba || 0,
            d.B.erroresPorPrueba || 0,
            0.01
        );
        pintarGrafico("chart-errores", [
            {
                etiqueta: "A",
                valor: d.A.erroresPorPrueba,
                clase: "bar-a",
                texto: window.Trabajo2Metrics.formatearNumero(d.A.erroresPorPrueba)
            },
            {
                etiqueta: "B",
                valor: d.B.erroresPorPrueba,
                clase: "bar-b",
                texto: window.Trabajo2Metrics.formatearNumero(d.B.erroresPorPrueba)
            }
        ], "Media de errores por prueba: version A " +
            window.Trabajo2Metrics.formatearNumero(d.A.erroresPorPrueba) +
            ", version B " + window.Trabajo2Metrics.formatearNumero(d.B.erroresPorPrueba));

        pintarGrafico("chart-eficiencia", [
            {
                etiqueta: "A",
                valor: d.A.eficiencia,
                clase: "bar-neutral",
                texto: window.Trabajo2Metrics.formatearNumero(d.A.eficiencia, 3)
            },
            {
                etiqueta: "B",
                valor: d.B.eficiencia,
                clase: "bar-b",
                texto: window.Trabajo2Metrics.formatearNumero(d.B.eficiencia, 3)
            }
        ], "Indice de eficiencia: version A " + window.Trabajo2Metrics.formatearNumero(d.A.eficiencia, 3) +
            ", version B " + window.Trabajo2Metrics.formatearNumero(d.B.eficiencia, 3));
    }

    function diferenciaTexto(a, b, sufijo, decimales) {
        if (a === null || b === null || a === undefined || b === undefined) {
            return "—";
        }
        const diferencia = b - a;
        const signo = diferencia > 0 ? "+" : "";
        return signo + diferencia.toFixed(decimales) + sufijo;
    }

    function renderizarComparativa(d) {
        const cuerpo = $("#tabla-comparativa tbody");
        if (!cuerpo) return;
        cuerpo.replaceChildren();
        const filas = [
            ["Pruebas iniciadas", d.A.total, d.B.total, "", 0],
            ["Participantes distintos", d.A.participantes, d.B.participantes, "", 0],
            ["Tiempo medio de búsqueda", d.A.mediaTiempo, d.B.mediaTiempo, " s", 2],
            ["Mediana del tiempo", d.A.medianaTiempo, d.B.medianaTiempo, " s", 2],
            ["Tasa de éxito", d.A.tasaExito, d.B.tasaExito, " pp", 1],
            ["Errores totales", d.A.erroresTotal, d.B.erroresTotal, "", 0],
            ["Media de errores por prueba", d.A.erroresPorPrueba, d.B.erroresPorPrueba, "", 2],
            ["Media de errores por participante", d.A.erroresPorParticipante, d.B.erroresPorParticipante, "", 2],
            ["Participantes con al menos un error", d.A.participantesConError, d.B.participantesConError, "", 0],
            ["Tasa de abandono", d.A.tasaAbandono, d.B.tasaAbandono, " pp", 1],
            ["Búsquedas medias (éxitos)", d.A.mediaBusquedas, d.B.mediaBusquedas, "", 2],
            ["Tiempo hasta el primer error", d.A.primerErrorMedio, d.B.primerErrorMedio, " s", 2],
            ["Tiempo de recuperación", d.A.recuperacionMedia, d.B.recuperacionMedia, " s", 2],
            ["Éxitos sin ningún error", d.A.sinErroresPct, d.B.sinErroresPct, " pp", 1],
            ["Índice de eficiencia", d.A.eficiencia, d.B.eficiencia, "", 3]
        ];
        for (const [nombre, valorA, valorB, sufijo, decimales] of filas) {
            const tr = document.createElement("tr");
            const th = document.createElement("th");
            th.scope = "row";
            th.textContent = nombre;
            const tdA = document.createElement("td");
            tdA.className = "num";
            tdA.textContent = valorA === null || valorA === undefined ? "—" :
                (decimales ? Number(valorA).toFixed(decimales) + sufijo : String(valorA) + sufijo);
            const tdB = document.createElement("td");
            tdB.className = "num strong";
            tdB.textContent = valorB === null || valorB === undefined ? "—" :
                (decimales ? Number(valorB).toFixed(decimales) + sufijo : String(valorB) + sufijo);
            const tdD = document.createElement("td");
            tdD.className = "num";
            tdD.textContent = diferenciaTexto(valorA, valorB, sufijo, decimales);
            tr.append(th, tdA, tdB, tdD);
            cuerpo.appendChild(tr);
        }

        texto("comparativa-nota",
            "El tiempo medio se calcula solo con pruebas completadas con exito. " +
            "Las tasas se calculan sobre todas las pruebas iniciadas; las abandonadas no cuentan como exito. " +
            "El indice de eficiencia es (tasa de exito / 100) / tiempo medio × 100; es complementario, no sustituye a las metricas principales.");
    }

    function renderizarOrdenes(d) {
        const cuerpo = $("#tabla-orden tbody");
        if (!cuerpo) return;
        cuerpo.replaceChildren();
        for (const clave of ["A-B", "B-A"]) {
            const grupo = d.ordenes[clave];
            const tr = document.createElement("tr");
            const th = document.createElement("th");
            th.scope = "row";
            th.textContent = clave === "A-B" ? "A y después B" : "B y después A";
            const tdParticipantes = document.createElement("td");
            tdParticipantes.className = "num";
            tdParticipantes.textContent = String(grupo.participantes);
            const tdPruebas = document.createElement("td");
            tdPruebas.className = "num";
            tdPruebas.textContent = String(grupo.pruebas);
            const tdA = document.createElement("td");
            tdA.className = "num";
            tdA.textContent = window.Trabajo2Metrics.formatearSegundos(grupo.A.mediaTiempo);
            const tdB = document.createElement("td");
            tdB.className = "num";
            tdB.textContent = window.Trabajo2Metrics.formatearSegundos(grupo.B.mediaTiempo);
            tr.append(th, tdParticipantes, tdPruebas, tdA, tdB);
            cuerpo.appendChild(tr);
        }
        mostrar("bloque-orden", d.ordenes["A-B"].pruebas > 0 || d.ordenes["B-A"].pruebas > 0);
    }

    function renderizarConclusiones(d) {
        const lista = $("#conclusiones");
        if (!lista) return;
        lista.replaceChildren();
        const conclusiones = window.Trabajo2Metrics.generarConclusiones(d, { demo: estado.demo });
        for (const textoConclusion of conclusiones) {
            const li = document.createElement("li");
            li.textContent = textoConclusion;
            lista.appendChild(li);
        }
    }

    function estadoPrueba(registro) {
        if (window.Trabajo2Metrics.esExito(registro)) return "Completada";
        if (registro.abandonada) return "Abandonada";
        if (!registro.completada) return "Incompleta";
        return "No válida";
    }

    function renderizarTablaIndividual(d) {
        const cuerpo = $("#tabla-individual tbody");
        if (!cuerpo) return;
        cuerpo.replaceChildren();
        let pruebas = [...d.pruebas].sort((a, b) => String(b.inicio).localeCompare(String(a.inicio)));
        if (estado.filtro) {
            pruebas = pruebas.filter((p) => p.participante === estado.filtro);
            texto("filtro-texto", "Mostrando solo las pruebas de " + estado.filtro +
                ". Las métricas agregadas siguen calculándose con todos los datos.");
        }
        const filas = pruebas.length ? pruebas : d.pruebas;
        for (const p of filas) {
            const tr = document.createElement("tr");
            const celdas = [
                p.participante,
                p.version,
                p.orden,
                "Habitación 103",
                window.Trabajo2Metrics.esExito(p) ? "Sí" : "No",
                p.duracionSeg.toFixed(2) + " s",
                String(p.errores),
                String(p.busquedas),
                p.primerErrorMs === null ? "—" : (p.primerErrorMs / 1000).toFixed(2) + " s",
                p.recuperacionMs === null ? "—" : (p.recuperacionMs / 1000).toFixed(2) + " s",
                estadoPrueba(p),
                formatearFechaCorta(p.inicio)
            ];
            celdas.forEach((valor, indice) => {
                const td = document.createElement("td");
                if ([5, 6, 7, 8, 9].includes(indice)) {
                    td.className = "num";
                }
                if (indice === 1) {
                    const insignia = document.createElement("span");
                    insignia.className = "badge " + (p.version === "B" ? "badge-b" : "badge-a");
                    insignia.textContent = p.version;
                    td.appendChild(insignia);
                } else if (indice === 4) {
                    const insignia = document.createElement("span");
                    insignia.className = "badge " + (valor === "Sí" ? "badge-success" : "badge-danger");
                    insignia.textContent = valor;
                    td.appendChild(insignia);
                } else if (indice === 10) {
                    const clase = valor === "Completada" ? "badge-success" :
                        (valor === "Abandonada" ? "badge-danger" : "badge-warn");
                    const insignia = document.createElement("span");
                    insignia.className = "badge " + clase;
                    insignia.textContent = valor;
                    td.appendChild(insignia);
                } else {
                    td.textContent = valor;
                }
                tr.appendChild(td);
            });
            cuerpo.appendChild(tr);
        }
        texto("tabla-individual-count", pruebas.length + " de " + d.pruebas.length + " prueba(s) mostradas.");
    }

    /* ---------- Acciones ---------- */
    function exportarCsv() {
        if (!estado.datos || !estado.datos.global.totalPruebas) return;
        const csv = "\uFEFF" + window.Trabajo2Metrics.aCsv(estado.pruebas);
        const sufijo = estado.demo ? "-DEMO" : (estado.modo === "local" ? "-local" : "");
        descargar("resultados-usabilidad" + sufijo + "-" + fechaArchivo() + ".csv",
            csv, "text/csv;charset=utf-8;");
    }

    function exportarJson() {
        if (!estado.datos || !estado.datos.global.totalPruebas) return;
        const sufijo = estado.demo ? "-DEMO" : (estado.modo === "local" ? "-local" : "");
        descargar("resultados-usabilidad" + sufijo + "-" + fechaArchivo() + ".json",
            JSON.stringify(estado.pruebas, null, 2), "application/json;charset=utf-8;");
    }

    async function importarArchivo(archivo) {
        const salida = $("#import-resultado");
        const subir = $("#importar-subir");
        salida.hidden = false;
        salida.className = "alert alert-info";
        salida.textContent = "Leyendo " + archivo.name + "…";
        try {
            const contenido = await archivo.text();
            const filas = window.Trabajo2Metrics.parseCsv(contenido);
            const validas = filas.map((f) => window.Trabajo2Metrics.normalizar(f)).filter(Boolean);
            const invalidas = filas.length - validas.length;
            if (!validas.length) {
                salida.className = "alert alert-danger";
                salida.textContent = "No se encontro ninguna prueba valida en el CSV. Revisa la cabecera.";
                return;
            }
            let subido = false;
            if (subir && subir.checked) {
                try {
                    await window.Trabajo2Api.ping();
                    const resultado = await window.Trabajo2Api.subirPruebas(validas);
                    salida.className = "alert alert-success";
                    salida.textContent = "Importacion al servidor: " + resultado.enviadas +
                        " anadida(s), " + resultado.duplicadas + " duplicada(s), " +
                        resultado.fallidas + " fallida(s).";
                    subido = true;
                } catch (e) {
                    salida.className = "alert alert-warning";
                    salida.textContent = "El servidor no responde. Las pruebas se guardaran solo en este navegador.";
                }
            }
            if (!subido) {
                const resultado = window.Trabajo2Api.guardarImportadas(validas);
                salida.className = "alert alert-success";
                salida.textContent = "Importacion local: " + resultado.anadidas +
                    " anadida(s), " + resultado.duplicadas + " duplicada(s)." +
                    (invalidas ? " " + invalidas + " fila(s) no valida(s) ignorada(s)." : "");
                estado.modo = "local";
                estado.demo = false;
                estado.pruebas = window.Trabajo2Api.pruebasImportadas();
                finalizarCarga();
            } else {
                await cargar();
            }
        } catch (e) {
            salida.className = "alert alert-danger";
            salida.textContent = "No se pudo leer el archivo: " + e.message;
        } finally {
            const entrada = $("#archivo-csv");
            if (entrada) entrada.value = "";
        }
    }

    function prepararEventos() {
        $("#btn-actualizar")?.addEventListener("click", cargar);
        $("#btn-exportar")?.addEventListener("click", exportarCsv);
        $("#btn-exportar-json")?.addEventListener("click", exportarJson);
        $("#btn-imprimir")?.addEventListener("click", () => window.print());
        const abrirDemo = () => {
            const url = new URL(window.location.href);
            url.searchParams.set("demo", "1");
            window.location.href = url.toString();
        };
        $("#btn-demo")?.addEventListener("click", abrirDemo);
        $("#btn-demo-vacio")?.addEventListener("click", abrirDemo);
        $("#btn-salir-demo")?.addEventListener("click", () => {
            const url = new URL(window.location.href);
            url.searchParams.delete("demo");
            url.searchParams.delete("participante");
            window.location.href = url.toString();
        });
        $("#btn-quitar-filtro")?.addEventListener("click", () => {
            const url = new URL(window.location.href);
            url.searchParams.delete("participante");
            window.location.href = url.toString();
        });
        $("#archivo-csv")?.addEventListener("change", (evento) => {
            const archivo = evento.target.files && evento.target.files[0];
            if (archivo) {
                importarArchivo(archivo);
            }
        });
        $("#btn-limpiar-importadas")?.addEventListener("click", () => {
            if (!window.confirm("¿Borrar las pruebas importadas guardadas en este navegador? Esta accion no afecta al servidor.")) {
                return;
            }
            window.Trabajo2Api.limpiarImportadas();
            estado.pruebas = [];
            estado.modo = "vacio";
            finalizarCarga();
        });
        document.addEventListener("trabajo2:api-cambiada", cargar);
    }

    document.addEventListener("DOMContentLoaded", () => {
        prepararEventos();
        cargar();
    });
})();
