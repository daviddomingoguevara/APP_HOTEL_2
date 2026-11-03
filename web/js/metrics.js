/* ============================================================
   Calculo de metricas de usabilidad y utilidades CSV.
   No hay valores inventados: todo sale de las pruebas recibidas.
   ============================================================ */
(function () {
    "use strict";

    const CABECERA = [
        "registrado_en", "id", "participante", "version", "orden", "inicio", "fin",
        "duracion_ms", "duracion_seg", "busquedas", "errores", "exito", "completada",
        "abandonada", "resultado", "primer_error_ms", "recuperacion_ms", "eventos_json"
    ];

    function numero(valor) {
        if (valor === null || valor === undefined || valor === "") {
            return null;
        }
        if (typeof valor === "number") {
            return Number.isFinite(valor) ? valor : null;
        }
        const limpio = String(valor).trim().replace(/\s/g, "").replace(",", ".");
        if (limpio === "") {
            return null;
        }
        const n = Number(limpio);
        return Number.isFinite(n) ? n : null;
    }

    function entero(valor, porDefecto = 0) {
        const n = numero(valor);
        return n === null ? porDefecto : Math.max(0, Math.round(n));
    }

    function booleano(valor) {
        if (typeof valor === "boolean") {
            return valor;
        }
        if (typeof valor === "number") {
            return valor !== 0;
        }
        const texto = String(valor || "").trim().toLowerCase();
        return ["true", "1", "si", "sí", "yes", "y", "x"].includes(texto);
    }

    function texto(valor) {
        return valor === null || valor === undefined ? "" : String(valor).trim();
    }

    function primerValor(obj, claves) {
        for (const clave of claves) {
            if (obj[clave] !== undefined && obj[clave] !== null && obj[clave] !== "") {
                return obj[clave];
            }
        }
        return undefined;
    }

    /**
     * Convierte un registro de la API o del CSV a un formato comun.
     * Devuelve null si el registro no sirve para calcular metricas.
     */
    function normalizar(bruto) {
        if (!bruto || typeof bruto !== "object") {
            return null;
        }
        const participante = texto(primerValor(bruto, ["participante", "participant"]));
        const version = texto(primerValor(bruto, ["version", "versión"])).toUpperCase();
        if (!/^[A-Za-z0-9_-]{2,20}$/.test(participante) || !["A", "B"].includes(version)) {
            return null;
        }
        const ms = numero(primerValor(bruto, ["duracionMs", "duracion_ms", "duración_ms"]));
        let seg = numero(primerValor(bruto, ["duracionSeg", "duracion_seg", "duración_seg"]));
        if (seg === null && ms !== null) {
            seg = ms / 1000;
        }
        if (seg === null && ms === null) {
            return null;
        }
        if (seg === null) {
            seg = 0;
        }
        if (seg < 0) {
            return null;
        }
        const ordenBruto = texto(primerValor(bruto, ["orden", "order"]));
        const orden = ["A-B", "B-A"].includes(ordenBruto) ? ordenBruto : "no-disponible";
        const exito = booleano(primerValor(bruto, ["exito", "éxito", "success"]));
        const completada = booleano(primerValor(bruto, ["completada", "completed"]));
        const abandonada = booleano(primerValor(bruto, ["abandonada", "abandoned"]));
        const primerError = numero(primerValor(bruto, ["primerErrorMs", "primer_error_ms"]));
        const recuperacion = numero(primerValor(bruto, ["recuperacionMs", "recuperacion_ms"]));
        const salida = {
            id: texto(primerValor(bruto, ["id"])) || (participante + "|" + version + "|" + texto(primerValor(bruto, ["inicio"]))),
            participante,
            version,
            orden,
            inicio: texto(primerValor(bruto, ["inicio", "start"])),
            fin: texto(primerValor(bruto, ["fin", "end"])),
            duracionSeg: Math.round(seg * 100) / 100,
            duracionMs: ms === null ? Math.round(seg * 1000) : Math.round(ms),
            busquedas: entero(primerValor(bruto, ["busquedas", "búsquedas", "searches"])),
            errores: entero(primerValor(bruto, ["errores", "errors"])),
            exito,
            completada,
            abandonada,
            resultado: texto(primerValor(bruto, ["resultado", "result"])) || "sin-resultado",
            primerErrorMs: primerError,
            recuperacionMs: recuperacion,
            registradoEn: texto(primerValor(bruto, ["registradoEn", "registrado_en", "recibidoEn"])),
            eventos: Array.isArray(bruto.eventos) ? bruto.eventos : null
        };
        if (salida.exito && salida.abandonada) {
            salida.exito = false;
        }
        return salida;
    }

    function esExito(registro) {
        return Boolean(registro.exito && registro.completada && !registro.abandonada);
    }

    function media(valores) {
        if (!valores.length) {
            return null;
        }
        return valores.reduce((a, b) => a + b, 0) / valores.length;
    }

    function mediana(valores) {
        if (!valores.length) {
            return null;
        }
        const ordenados = [...valores].sort((a, b) => a - b);
        const mitad = Math.floor(ordenados.length / 2);
        return ordenados.length % 2 === 0
            ? (ordenados[mitad - 1] + ordenados[mitad]) / 2
            : ordenados[mitad];
    }

    function redondear(valor, decimales = 2) {
        if (valor === null || valor === undefined || !Number.isFinite(valor)) {
            return null;
        }
        const factor = Math.pow(10, decimales);
        return Math.round(valor * factor) / factor;
    }

    function estadisticasVersion(registros) {
        const exitos = registros.filter(esExito);
        const tiempos = exitos.map((r) => r.duracionSeg);
        const mediaTiempo = media(tiempos);
        const primerosErrores = registros
            .filter((r) => r.primerErrorMs !== null && r.primerErrorMs !== undefined)
            .map((r) => r.primerErrorMs / 1000);
        const recuperaciones = registros
            .filter((r) => r.recuperacionMs !== null && r.recuperacionMs !== undefined)
            .map((r) => r.recuperacionMs / 1000);
        const participantes = new Set(registros.map((r) => r.participante));
        const abandonadas = registros.filter((r) => r.abandonada).length;
        const erroresTotal = registros.reduce((suma, r) => suma + r.errores, 0);
        const participantesConError = new Set(
            registros.filter((r) => r.errores > 0).map((r) => r.participante)
        ).size;
        const tasaExito = registros.length ? (exitos.length / registros.length) * 100 : null;
        const mediaBusquedas = media(exitos.map((r) => r.busquedas));
        const exitosSinError = exitos.filter((r) => r.errores === 0).length;
        const eficiencia = mediaTiempo && mediaTiempo > 0 && tasaExito !== null
            ? (tasaExito / 100) / mediaTiempo * 100
            : null;

        return {
            total: registros.length,
            participantes: participantes.size,
            exitos: exitos.length,
            completadas: registros.filter((r) => r.completada).length,
            abandonadas,
            tasaExito: redondear(tasaExito, 1),
            tasaAbandono: registros.length ? redondear((abandonadas / registros.length) * 100, 1) : null,
            mediaTiempo: redondear(mediaTiempo, 2),
            medianaTiempo: redondear(mediana(tiempos), 2),
            minTiempo: tiempos.length ? redondear(Math.min(...tiempos), 2) : null,
            maxTiempo: tiempos.length ? redondear(Math.max(...tiempos), 2) : null,
            erroresTotal,
            erroresPorPrueba: registros.length ? redondear(erroresTotal / registros.length, 2) : null,
            erroresPorParticipante: participantes.size ? redondear(erroresTotal / participantes.size, 2) : null,
            participantesConError,
            mediaBusquedas: redondear(mediaBusquedas, 2),
            primerErrorMedio: redondear(media(primerosErrores), 2),
            recuperacionMedia: redondear(media(recuperaciones), 2),
            sinErroresPct: exitos.length ? redondear((exitosSinError / exitos.length) * 100, 1) : null,
            eficiencia: redondear(eficiencia, 3)
        };
    }

    function calcular(pruebas) {
        const validas = [];
        let invalidas = 0;
        for (const bruta of pruebas || []) {
            const normalizada = normalizar(bruta);
            if (normalizada) {
                validas.push(normalizada);
            } else {
                invalidas++;
            }
        }
        validas.sort((a, b) => String(a.inicio).localeCompare(String(b.inicio)));

        const registrosA = validas.filter((r) => r.version === "A");
        const registrosB = validas.filter((r) => r.version === "B");
        const statsA = estadisticasVersion(registrosA);
        const statsB = estadisticasVersion(registrosB);
        const participantes = new Set(validas.map((r) => r.participante));
        const conA = new Set(registrosA.map((r) => r.participante));
        const conB = new Set(registrosB.map((r) => r.participante));
        const conAmbas = [...participantes].filter((p) => conA.has(p) && conB.has(p));

        const exitosA = new Map(registrosA.filter(esExito).map((r) => [r.participante, r]));
        const exitosB = new Map(registrosB.filter(esExito).map((r) => [r.participante, r]));
        const pares = [];
        for (const participante of conAmbas) {
            const a = exitosA.get(participante);
            const b = exitosB.get(participante);
            if (a && b && a.duracionSeg > 0) {
                pares.push({
                    participante,
                    a: a.duracionSeg,
                    b: b.duracionSeg,
                    mejora: (a.duracionSeg - b.duracionSeg) / a.duracionSeg * 100
                });
            }
        }

        const ordenA_B = validas.filter((r) => r.orden === "A-B");
        const ordenB_A = validas.filter((r) => r.orden === "B-A");

        const mejoraTiempo = statsA.mediaTiempo && statsB.mediaTiempo
            ? redondear((statsA.mediaTiempo - statsB.mediaTiempo) / statsA.mediaTiempo * 100, 1)
            : null;
        const mejoraEmparejada = pares.length
            ? redondear(media(pares.map((p) => p.mejora)), 1)
            : null;
        const diferenciaExito = statsA.tasaExito !== null && statsB.tasaExito !== null
            ? redondear(statsB.tasaExito - statsA.tasaExito, 1)
            : null;
        const diferenciaErrores = statsA.erroresPorPrueba !== null && statsB.erroresPorPrueba !== null
            ? redondear(statsB.erroresPorPrueba - statsA.erroresPorPrueba, 2)
            : null;

        const fechas = validas
            .map((r) => r.inicio)
            .filter(Boolean)
            .sort();

        return {
            pruebas: validas,
            invalidas,
            global: {
                totalPruebas: validas.length,
                participantes: participantes.size,
                participantesConAmbas: conAmbas.length,
                participantesConAmbasExito: pares.length,
                exitos: validas.filter(esExito).length,
                abandonos: validas.filter((r) => r.abandonada).length,
                errores: validas.reduce((suma, r) => suma + r.errores, 0),
                tasaExito: validas.length
                    ? redondear((validas.filter(esExito).length / validas.length) * 100, 1)
                    : null,
                tasaAbandono: validas.length
                    ? redondear((validas.filter((r) => r.abandonada).length / validas.length) * 100, 1)
                    : null,
                primeraFecha: fechas[0] || null,
                ultimaFecha: fechas[fechas.length - 1] || null
            },
            A: statsA,
            B: statsB,
            mejoraTiempo,
            mejoraEmparejada,
            pares,
            diferenciaExito,
            diferenciaErrores,
            ordenes: {
                "A-B": {
                    pruebas: ordenA_B.length,
                    participantes: new Set(ordenA_B.map((r) => r.participante)).size,
                    A: estadisticasVersion(ordenA_B.filter((r) => r.version === "A")),
                    B: estadisticasVersion(ordenA_B.filter((r) => r.version === "B"))
                },
                "B-A": {
                    pruebas: ordenB_A.length,
                    participantes: new Set(ordenB_A.map((r) => r.participante)).size,
                    A: estadisticasVersion(ordenB_A.filter((r) => r.version === "A")),
                    B: estadisticasVersion(ordenB_A.filter((r) => r.version === "B"))
                }
            }
        };
    }

    function generarConclusiones(datos, opciones = {}) {
        const conclusiones = [];
        if (!datos || datos.global.totalPruebas === 0) {
            return ["Todavia no hay pruebas registradas. Las conclusiones apareceran cuando existan datos reales."];
        }
        const g = datos.global;
        const demo = Boolean(opciones.demo);
        if (demo) {
            conclusiones.push("Estos datos son ficticios y se incluyen solo para demostrar el funcionamiento de la pagina. No deben citarse como resultados reales.");
        }
        conclusiones.push(
            "La muestra contiene " + g.participantes + " participante(s) y " + g.totalPruebas +
            " prueba(s). Con menos de 10 participantes los resultados son descriptivos y no permiten generalizar."
        );

        if (!datos.A.total || !datos.B.total) {
            conclusiones.push("Faltan pruebas de una de las dos versiones, por lo que no se puede comparar el tiempo medio todavia.");
        } else if (datos.mejoraTiempo !== null) {
            if (datos.mejoraTiempo > 0) {
                conclusiones.push(
                    "En esta muestra, la version B tardo un " + Math.abs(datos.mejoraTiempo).toFixed(1) +
                    " % menos de media en completar la tarea que la version A (" +
                    datos.A.mediaTiempo.toFixed(2) + " s frente a " + datos.B.mediaTiempo.toFixed(2) + " s)."
                );
            } else if (datos.mejoraTiempo < 0) {
                conclusiones.push(
                    "En esta muestra, la version B tardo un " + Math.abs(datos.mejoraTiempo).toFixed(1) +
                    " % mas que la version A. El resultado negativo se muestra sin ocultarlo."
                );
            } else {
                conclusiones.push("Las dos versiones obtuvieron el mismo tiempo medio en esta muestra.");
            }
        }

        if (datos.diferenciaExito !== null) {
            const signo = datos.diferenciaExito >= 0 ? "superior" : "inferior";
            conclusiones.push(
                "La tasa de exito de B fue " + Math.abs(datos.diferenciaExito).toFixed(1) +
                " puntos porcentuales " + signo + " a la de A (" +
                datos.A.tasaExito.toFixed(1) + " % frente a " + datos.B.tasaExito.toFixed(1) + " %)."
            );
        }

        if (datos.A.erroresPorPrueba !== null && datos.B.erroresPorPrueba !== null) {
            const masOMenos = datos.B.erroresPorPrueba <= datos.A.erroresPorPrueba ? "menos" : "mas";
            conclusiones.push(
                "La media de errores por prueba fue de " + datos.A.erroresPorPrueba.toFixed(2) +
                " en A y " + datos.B.erroresPorPrueba.toFixed(2) + " en B: B registro " +
                Math.abs(datos.diferenciaErrores).toFixed(2) + " errores " + masOMenos + " por prueba."
            );
        }

        if (datos.mejoraEmparejada !== null) {
            conclusiones.push(
                "Entre los " + datos.pares.length + " participantes que completaron ambas versiones, la mejora media " +
                "del tiempo fue del " + datos.mejoraEmparejada.toFixed(1) + " %. Esta comparacion reduce parte del " +
                "efecto de las diferencias individuales, pero sigue afectada por el aprendizaje."
            );
        }

        const grupos = datos.ordenes;
        if (grupos["A-B"].pruebas > 0 && grupos["B-A"].pruebas > 0) {
            const aPrimero = grupos["A-B"].B.mediaTiempo;
            const bPrimero = grupos["B-A"].B.mediaTiempo;
            if (aPrimero !== null && bPrimero !== null) {
                conclusiones.push(
                    "El orden de las pruebas puede influir: el tiempo medio de B fue " + aPrimero.toFixed(2) +
                    " s cuando se hizo despues de A y " + bPrimero.toFixed(2) +
                    " s cuando se hizo primero. Conviene tenerlo en cuenta al interpretar la mejora."
                );
            }
        }

        conclusiones.push(
            "Las diferencias observadas pueden estar relacionadas con las mejoras de la interfaz B " +
            "(mensajes claros, validacion, busqueda con Enter, foco visible y menos pasos), pero tambien con el " +
            "aprendizaje, la familiaridad con la tarea o la variabilidad entre personas. No se puede afirmar causalidad " +
            "con esta muestra."
        );
        conclusiones.push(
            "Las metricas no miden satisfaccion ni accesibilidad subjetiva. Para valorar si una interfaz es mejor " +
            "hay que considerar tambien los errores, la tasa de exito, la accesibilidad y la opinion de las personas."
        );
        return conclusiones;
    }

    /* ---------- CSV ---------- */
    function citaCsv(valor) {
        return '"' + String(valor === null || valor === undefined ? "" : valor).replace(/"/g, '""') + '"';
    }

    function aCsv(pruebas) {
        const filas = [CABECERA.map(citaCsv).join(",")];
        for (const p of pruebas) {
            const n = normalizar(p);
            if (!n) {
                continue;
            }
            const eventos = n.eventos ? JSON.stringify(n.eventos) : "[]";
            const fila = [
                n.registradoEn, n.id, n.participante, n.version, n.orden, n.inicio, n.fin,
                n.duracionMs, n.duracionSeg.toFixed(2), n.busquedas, n.errores,
                n.exito ? "true" : "false", n.completada ? "true" : "false",
                n.abandonada ? "true" : "false", n.resultado,
                n.primerErrorMs === null ? "" : n.primerErrorMs,
                n.recuperacionMs === null ? "" : n.recuperacionMs,
                eventos
            ];
            filas.push(fila.map(citaCsv).join(","));
        }
        return filas.join("\r\n") + "\r\n";
    }

    function detectarSeparador(texto) {
        const primera = texto.split(/\r?\n/, 1)[0] || "";
        const puntoYComa = (primera.match(/;/g) || []).length;
        const comas = (primera.match(/,/g) || []).length;
        return puntoYComa > comas ? ";" : ",";
    }

    function parseCsv(texto) {
        let contenido = String(texto || "");
        if (contenido.charCodeAt(0) === 0xFEFF) {
            contenido = contenido.slice(1);
        }
        const separador = detectarSeparador(contenido);
        const filas = [];
        let fila = [];
        let campo = "";
        let entreComillas = false;

        for (let i = 0; i < contenido.length; i++) {
            const c = contenido[i];
            if (entreComillas) {
                if (c === '"') {
                    if (contenido[i + 1] === '"') {
                        campo += '"';
                        i++;
                    } else {
                        entreComillas = false;
                    }
                } else {
                    campo += c;
                }
            } else if (c === '"') {
                entreComillas = true;
            } else if (c === separador) {
                fila.push(campo);
                campo = "";
            } else if (c === "\n" || c === "\r") {
                if (c === "\r" && contenido[i + 1] === "\n") {
                    i++;
                }
                fila.push(campo);
                campo = "";
                if (fila.some((v) => String(v).trim() !== "")) {
                    filas.push(fila);
                }
                fila = [];
            } else {
                campo += c;
            }
        }
        if (campo !== "" || fila.length) {
            fila.push(campo);
            if (fila.some((v) => String(v).trim() !== "")) {
                filas.push(fila);
            }
        }
        if (!filas.length) {
            return [];
        }
        const cabecera = filas.shift().map((h) => h.trim().toLowerCase().replace(/\s+/g, "_"));
        return filas.map((valores) => {
            const obj = {};
            cabecera.forEach((nombre, indice) => {
                obj[nombre] = valores[indice] !== undefined ? valores[indice] : "";
            });
            return obj;
        });
    }

    function formatearSegundos(valor) {
        return valor === null || valor === undefined ? "—" : valor.toFixed(2) + " s";
    }

    function formatearPct(valor, decimales = 1) {
        return valor === null || valor === undefined ? "—" : valor.toFixed(decimales) + " %";
    }

    function formatearNumero(valor, decimales = 2) {
        return valor === null || valor === undefined ? "—" : valor.toFixed(decimales);
    }

    window.Trabajo2Metrics = {
        CABECERA,
        normalizar,
        calcular,
        generarConclusiones,
        aCsv,
        parseCsv,
        redondear,
        formatearSegundos,
        formatearPct,
        formatearNumero,
        esExito
    };
})();
