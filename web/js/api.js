/* ============================================================
   Capa de acceso a datos del experimento.
   - Si el servidor local esta disponible, usa la API REST.
   - Si no, permite un modo local basado en localStorage + CSV.
   ============================================================ */
(function () {
    "use strict";

    const CLAVE_API = "trabajo2_api_base";
    const CLAVE_PARTICIPANTES = "trabajo2_participantes_local";
    const CLAVE_IMPORTADAS = "trabajo2_pruebas_importadas";

    function normalizarBase(valor) {
        return String(valor || "").trim().replace(/\/+$/, "");
    }

    function basePorDefecto() {
        const params = new URLSearchParams(window.location.search);
        if (params.has("api")) {
            return normalizarBase(params.get("api"));
        }
        let guardada = null;
        try {
            guardada = window.localStorage.getItem(CLAVE_API);
        } catch (e) {
            guardada = null;
        }
        if (guardada !== null) {
            return normalizarBase(guardada);
        }
        const local = ["localhost", "127.0.0.1", "::1"].includes(window.location.hostname);
        if (local && window.location.port === "8080") {
            return "";
        }
        return "http://localhost:8080";
    }

    let base = basePorDefecto();

    function url(ruta) {
        return base + ruta;
    }

    function baseVisible() {
        if (base === "") {
            return window.location.origin;
        }
        return base;
    }

    function guardarBase(nueva) {
        base = normalizarBase(nueva);
        try {
            window.localStorage.setItem(CLAVE_API, base);
        } catch (e) {
            // Sin almacenamiento: la base seguira activa en esta pestana.
        }
        return base;
    }

    async function peticion(ruta, opciones = {}, tiempoMaximo = 3500) {
        const controlador = new AbortController();
        const temporizador = window.setTimeout(() => controlador.abort(), tiempoMaximo);
        try {
            const respuesta = await fetch(url(ruta), {
                ...opciones,
                signal: controlador.signal,
                headers: {
                    Accept: "application/json",
                    ...(opciones.headers || {})
                }
            });
            const texto = await respuesta.text();
            let datos = null;
            if (texto) {
                try {
                    datos = JSON.parse(texto);
                } catch (e) {
                    datos = { error: texto };
                }
            }
            if (!respuesta.ok) {
                const error = new Error((datos && datos.error) || ("Error HTTP " + respuesta.status));
                error.status = respuesta.status;
                error.datos = datos;
                throw error;
            }
            return datos;
        } finally {
            window.clearTimeout(temporizador);
        }
    }

    async function ping() {
        return peticion("/api/salud", { method: "GET" }, 1800);
    }

    async function crearParticipante(id) {
        const datos = await peticion("/api/participantes", {
            method: "POST",
            headers: { "Content-Type": "application/json; charset=utf-8" },
            body: JSON.stringify({ id })
        });
        return { ...datos, modo: "servidor" };
    }

    async function obtenerParticipante(id) {
        return peticion("/api/participantes/" + encodeURIComponent(id), { method: "GET" });
    }

    async function obtenerPruebas() {
        const datos = await peticion("/api/pruebas", { method: "GET" }, 5000);
        return Array.isArray(datos) ? datos : [];
    }

    async function enviarPrueba(prueba) {
        return peticion("/api/pruebas", {
            method: "POST",
            headers: { "Content-Type": "application/json; charset=utf-8" },
            body: JSON.stringify(prueba)
        }, 5000);
    }

    async function subirPruebas(pruebas) {
        let enviadas = 0;
        let duplicadas = 0;
        let fallidas = 0;
        for (const prueba of pruebas) {
            try {
                const respuesta = await enviarPrueba(prueba);
                if (respuesta && respuesta.duplicado) {
                    duplicadas++;
                } else {
                    enviadas++;
                }
            } catch (e) {
                fallidas++;
            }
        }
        return { enviadas, duplicadas, fallidas };
    }

    /* ---------- Modo local ---------- */
    function leerJson(clave, valorDefecto) {
        try {
            const bruto = window.localStorage.getItem(clave);
            if (!bruto) {
                return valorDefecto;
            }
            const datos = JSON.parse(bruto);
            return datos;
        } catch (e) {
            return valorDefecto;
        }
    }

    function escribirJson(clave, datos) {
        try {
            window.localStorage.setItem(clave, JSON.stringify(datos));
            return true;
        } catch (e) {
            return false;
        }
    }

    function participantesLocales() {
        const lista = leerJson(CLAVE_PARTICIPANTES, []);
        return Array.isArray(lista) ? lista : [];
    }

    function crearParticipanteLocal(id) {
        const lista = participantesLocales();
        const existente = lista.find((p) => p.id === id);
        if (existente) {
            return { ...existente, modo: "local" };
        }
        const ab = lista.filter((p) => p.orden === "A-B").length;
        const ba = lista.filter((p) => p.orden === "B-A").length;
        const orden = ab <= ba ? "A-B" : "B-A";
        const participante = { id, orden, creadoEn: new Date().toISOString(), modo: "local" };
        lista.push(participante);
        escribirJson(CLAVE_PARTICIPANTES, lista);
        return participante;
    }

    function pruebasImportadas() {
        const lista = leerJson(CLAVE_IMPORTADAS, []);
        return Array.isArray(lista) ? lista : [];
    }

    function guardarImportadas(pruebas) {
        const actuales = pruebasImportadas();
        const porId = new Map();
        for (const p of actuales) {
            const clave = p.id || (p.participante + "|" + p.version + "|" + p.inicio);
            porId.set(clave, p);
        }
        let anadidas = 0;
        let duplicadas = 0;
        for (const p of pruebas) {
            const clave = p.id || (p.participante + "|" + p.version + "|" + p.inicio);
            if (porId.has(clave)) {
                duplicadas++;
            } else {
                porId.set(clave, p);
                anadidas++;
            }
        }
        escribirJson(CLAVE_IMPORTADAS, Array.from(porId.values()));
        return { anadidas, duplicadas };
    }

    function limpiarImportadas() {
        try {
            window.localStorage.removeItem(CLAVE_IMPORTADAS);
        } catch (e) {
            // Nada que hacer.
        }
    }

    window.Trabajo2Api = {
        url,
        base: () => base,
        baseVisible,
        guardarBase,
        ping,
        crearParticipante,
        obtenerParticipante,
        obtenerPruebas,
        enviarPrueba,
        subirPruebas,
        crearParticipanteLocal,
        pruebasImportadas,
        guardarImportadas,
        limpiarImportadas
    };
})();
