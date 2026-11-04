/* ============================================================
   Logica comun de la web: navegacion, estado del servidor,
   registro de participantes y comprobacion de progreso.
   ============================================================ */
(function () {
    "use strict";

    const CLAVE_PARTICIPANTE = "trabajo2_participante";

    function $(selector, raiz = document) {
        return raiz.querySelector(selector);
    }

    function $todos(selector, raiz = document) {
        return Array.from(raiz.querySelectorAll(selector));
    }

    function leerParticipanteGuardado() {
        try {
            const datos = JSON.parse(window.localStorage.getItem(CLAVE_PARTICIPANTE) || "null");
            return datos && datos.id ? datos : null;
        } catch (e) {
            return null;
        }
    }

    function guardarParticipante(datos) {
        try {
            window.localStorage.setItem(CLAVE_PARTICIPANTE, JSON.stringify(datos));
        } catch (e) {
            // Sin almacenamiento persistente.
        }
    }

    function textoOrden(orden) {
        if (orden === "A-B") {
            return "Primero la version A (basica) y despues la version B (mejorada).";
        }
        if (orden === "B-A") {
            return "Primero la version B (mejorada) y despues la version A (basica).";
        }
        return "Orden no disponible. Comprueba la conexion con el servidor.";
    }

    function nombreCortoOrden(orden) {
        if (orden === "A-B") return "A → B";
        if (orden === "B-A") return "B → A";
        return "Sin asignar";
    }

    function comandoPrimeraApp(orden) {
        return orden === "B-A" ? ".\\scripts\\ejecutar-mejorada.ps1" : ".\\scripts\\ejecutar-basica.ps1";
    }

    function actualizarEstadoApi(estado, datos) {
        $todos("[data-api-status]").forEach((elemento) => {
            elemento.dataset.state = estado;
            if (estado === "ok") {
                const total = datos && typeof datos.pruebas === "number" ? " · " + datos.pruebas + " pruebas" : "";
                elemento.textContent = "Servidor conectado" + total;
                elemento.title = "Servidor local disponible en " + window.Trabajo2Api.baseVisible();
            } else if (estado === "offline") {
                elemento.textContent = "Modo sin conexión";
                elemento.title = "No se pudo conectar con " + window.Trabajo2Api.baseVisible();
            } else {
                elemento.textContent = "Comprobando servidor…";
                elemento.title = "Contactando con " + window.Trabajo2Api.baseVisible();
            }
        });
    }

    async function comprobarServidor() {
        actualizarEstadoApi("loading");
        try {
            const datos = await window.Trabajo2Api.ping();
            actualizarEstadoApi("ok", datos);
            return true;
        } catch (e) {
            actualizarEstadoApi("offline");
            return false;
        }
    }

    function prepararNavegacion() {
        const pagina = document.body.dataset.page;
        $todos(".site-nav a[data-nav]").forEach((enlace) => {
            if (enlace.dataset.nav === pagina) {
                enlace.setAttribute("aria-current", "page");
            } else {
                enlace.removeAttribute("aria-current");
            }
        });
        const anio = $("#anio");
        if (anio) {
            anio.textContent = String(new Date().getFullYear());
        }
    }

    function prepararCopiar() {
        document.addEventListener("click", async (evento) => {
            const boton = evento.target.closest("[data-copy]");
            if (!boton) {
                return;
            }
            evento.preventDefault();
            const selector = boton.dataset.copy;
            const destino = selector ? document.querySelector(selector) : null;
            const texto = destino ? (destino.textContent || destino.value || "") : (boton.dataset.copyText || "");
            if (!texto) {
                return;
            }
            try {
                await navigator.clipboard.writeText(texto.trim());
                const original = boton.textContent;
                boton.textContent = "Copiado";
                window.setTimeout(() => { boton.textContent = original; }, 1600);
            } catch (e) {
                window.prompt("Copia este texto:", texto.trim());
            }
        });
    }

    function prepararAjustesApi() {
        const formulario = $("[data-api-settings]");
        if (!formulario) {
            return;
        }
        const campo = $("[name='apiBase']", formulario);
        const salida = $("[data-api-result]", formulario);
        const botonProbar = $("[data-api-test]", formulario);
        const botonGuardar = $("[data-api-save]", formulario);
        const botonLocal = $("[data-api-local]", formulario);
        campo.value = window.Trabajo2Api.base();

        function mensaje(texto, tipo) {
            salida.textContent = texto;
            salida.dataset.state = tipo || "";
        }

        async function probar(baseTemporal) {
            mensaje("Comprobando " + (baseTemporal || window.location.origin) + "…", "loading");
            const controlador = new AbortController();
            const temporizador = window.setTimeout(() => controlador.abort(), 2500);
            try {
                const respuesta = await fetch((baseTemporal || "") + "/api/salud", {
                    signal: controlador.signal,
                    headers: { Accept: "application/json" }
                });
                if (!respuesta.ok) {
                    throw new Error("HTTP " + respuesta.status);
                }
                const datos = await respuesta.json();
                mensaje("Conexion correcta. " + datos.pruebas + " prueba(s) y " +
                    datos.participantes + " participante(s).", "ok");
                return true;
            } catch (e) {
                mensaje("No se pudo conectar. Arranca el servidor con .\\scripts\\iniciar-servidor.ps1 " +
                    "o revisa la direccion.", "error");
                return false;
            } finally {
                window.clearTimeout(temporizador);
            }
        }

        botonProbar.addEventListener("click", () => probar(campo.value.trim().replace(/\/+$/, "")));
        botonGuardar.addEventListener("click", async () => {
            const base = window.Trabajo2Api.guardarBase(campo.value);
            mensaje("Direccion guardada: " + (base || window.location.origin), "info");
            const ok = await comprobarServidor();
            if (ok) {
                document.dispatchEvent(new CustomEvent("trabajo2:api-cambiada"));
            }
        });
        if (botonLocal) {
            botonLocal.addEventListener("click", async () => {
                campo.value = window.Trabajo2Api.guardarBase("http://localhost:8080");
                await comprobarServidor();
            });
        }
    }

    function filaProgreso(version, registros) {
        const fila = document.createElement("div");
        fila.className = "progress-row";
        const etiqueta = document.createElement("span");
        etiqueta.className = "progress-version";
        etiqueta.textContent = version === "A" ? "Version A (basica)" : "Version B (mejorada)";
        fila.appendChild(etiqueta);

        const insignia = document.createElement("span");
        if (!registros.length) {
            insignia.className = "badge badge-neutral";
            insignia.textContent = "Pendiente";
        } else {
            const metricas = window.Trabajo2Metrics;
            const exitosa = registros.find((r) => metricas.esExito(r));
            const abandonada = registros.find((r) => r.abandonada);
            if (exitosa) {
                insignia.className = "badge badge-success";
                insignia.textContent = "Completada · " + exitosa.duracionSeg.toFixed(2) + " s · " +
                    exitosa.errores + " error(es)";
            } else if (abandonada) {
                insignia.className = "badge badge-danger";
                insignia.textContent = "Abandonada";
            } else {
                insignia.className = "badge badge-warn";
                insignia.textContent = "Incompleta";
            }
        }
        fila.appendChild(insignia);
        return { fila, exitosa: registros.some((r) => window.Trabajo2Metrics.esExito(r)) };
    }

    function prepararRegistro() {
        const formulario = $("#registro-form");
        if (!formulario) {
            return;
        }
        const campo = $("#participante-id");
        const consentimiento = $("#consentimiento");
        const error = $("#registro-error");
        const boton = $("#registro-btn");
        const resultado = $("#registro-resultado");
        const guardado = leerParticipanteGuardado();
        if (guardado && !campo.value) {
            campo.value = guardado.id;
        }

        let participanteActual = guardado;

        function mostrarError(mensaje) {
            error.textContent = mensaje;
            error.hidden = !mensaje;
            if (mensaje) {
                campo.setAttribute("aria-invalid", "true");
                campo.focus();
            } else {
                campo.removeAttribute("aria-invalid");
            }
        }

        function rellenarResultado(participante, aviso) {
            participanteActual = participante;
            guardarParticipante(participante);
            resultado.hidden = false;
            $("#asignacion-codigo").textContent = participante.id;
            $("#asignacion-orden").textContent = nombreCortoOrden(participante.orden);
            $("#asignacion-grupo").textContent = textoOrden(participante.orden);
            $("#asignacion-modo").textContent = participante.modo === "servidor"
                ? "Conectado al servidor local: los resultados se enviaran automaticamente."
                : "Modo local: el servidor no responde. Los resultados de las aplicaciones se podran importar despues desde un CSV.";
            const avisoElemento = $("#asignacion-aviso");
            if (aviso) {
                avisoElemento.textContent = aviso;
                avisoElemento.hidden = false;
            } else {
                avisoElemento.hidden = true;
            }
            $("#asignacion-primera-app").textContent = comandoPrimeraApp(participante.orden);
            $("#progreso-resultado").hidden = true;
            $("#progreso-resultado").replaceChildren();
            resultado.scrollIntoView({ behavior: "smooth", block: "start" });
        }

        formulario.addEventListener("submit", async (evento) => {
            evento.preventDefault();
            const id = campo.value.trim();
            if (!/^[A-Za-z0-9_-]{2,20}$/.test(id)) {
                mostrarError("Usa entre 2 y 20 caracteres: letras, numeros, guion o guion bajo. No uses tu nombre ni tu correo.");
                return;
            }
            if (!consentimiento.checked) {
                mostrarError("Marca la casilla para confirmar que participas de forma anonima y has leido las instrucciones.");
                consentimiento.focus();
                return;
            }
            mostrarError("");
            boton.disabled = true;
            boton.textContent = "Registrando…";
            try {
                const participante = await window.Trabajo2Api.crearParticipante(id);
                actualizarEstadoApi("ok", await window.Trabajo2Api.ping().catch(() => null));
                rellenarResultado(participante, null);
            } catch (e) {
                const local = window.Trabajo2Api.crearParticipanteLocal(id);
                actualizarEstadoApi("offline");
                rellenarResultado(local,
                    "El servidor local no esta disponible. Se ha asignado el orden en modo local; " +
                    "puedes realizar las pruebas y guardar los CSV para importarlos despues.");
            } finally {
                boton.disabled = false;
                boton.textContent = "Obtener mi código y orden";
            }
        });

        const botonComprobar = $("#btn-comprobar");
        if (botonComprobar) {
            botonComprobar.addEventListener("click", async () => {
                if (!participanteActual) {
                    return;
                }
                botonComprobar.disabled = true;
                botonComprobar.textContent = "Comprobando…";
                const contenedor = $("#progreso-resultado");
                contenedor.replaceChildren();
                contenedor.hidden = false;
                let pruebas = [];
                let fuente = "";
                try {
                    pruebas = await window.Trabajo2Api.obtenerPruebas();
                    fuente = "servidor";
                    actualizarEstadoApi("ok");
                } catch (e) {
                    pruebas = window.Trabajo2Api.pruebasImportadas();
                    fuente = "local";
                    actualizarEstadoApi("offline");
                }
                const normalizadas = pruebas
                    .map((p) => window.Trabajo2Metrics.normalizar(p))
                    .filter(Boolean)
                    .filter((p) => p.participante === participanteActual.id);
                const titulo = document.createElement("h4");
                titulo.textContent = "Progreso de " + participanteActual.id;
                contenedor.appendChild(titulo);
                const a = filaProgreso("A", normalizadas.filter((p) => p.version === "A"));
                const b = filaProgreso("B", normalizadas.filter((p) => p.version === "B"));
                contenedor.appendChild(a.fila);
                contenedor.appendChild(b.fila);
                const nota = document.createElement("p");
                nota.className = "small muted";
                nota.textContent = fuente === "servidor"
                    ? "Datos consultados al servidor local."
                    : "Servidor no disponible: se muestran las pruebas importadas en este navegador.";
                contenedor.appendChild(nota);
                const enlace = $("#enlace-resultados");
                if (enlace) {
                    enlace.href = "resultados.html?participante=" + encodeURIComponent(participanteActual.id);
                    enlace.hidden = !(a.exitosa && b.exitosa);
                }
                botonComprobar.disabled = false;
                botonComprobar.textContent = "Comprobar mi progreso";
            });
        }

        const botonGuardado = $("#usar-codigo-guardado");
        if (botonGuardado && guardado) {
            botonGuardado.hidden = false;
            botonGuardado.textContent = "Usar el código " + guardado.id;
            botonGuardado.addEventListener("click", () => {
                campo.value = guardado.id;
                campo.focus();
            });
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        prepararNavegacion();
        prepararCopiar();
        prepararAjustesApi();
        prepararRegistro();
        comprobarServidor();
    });
})();
