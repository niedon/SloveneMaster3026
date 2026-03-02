<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcadaval.esloveno.beans.enums.TipoPalabra" %>
<%@ page import="com.bcadaval.esloveno.beans.enums.Transitividad" %>
<%@ page import="com.bcadaval.esloveno.beans.enums.Aspecto" %>
<%@ page import="com.bcadaval.esloveno.beans.enums.Genero" %>
<%@ page import="com.bcadaval.esloveno.beans.enums.TipoPronombre" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Buscar Palabras - Esloveno</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css?v=${cssVersion}">
    <%@ include file="head-favicon.jsp" %>
</head>
<body>
    <c:set var="pageTitle" value="🔍 Buscar Palabras" scope="request"/>
    <%@ include file="navbar.jsp" %>
    <div class="container container-extra-wide">
        <div class="buscar-layout">

            <!-- ========== PANEL IZQUIERDO: Búsqueda + Resultados ========== -->
            <div class="buscar-main">

                <!-- Barra de búsqueda -->
                <div class="buscar-search-bar">
                    <input type="text"
                           id="searchInput"
                           placeholder="🔍 Buscar palabra en esloveno o español..."
                           autocomplete="off">
                    <button class="btn-primary" onclick="buscar()">Buscar</button>
                </div>

                <!-- Filtros -->
                <div class="buscar-filtros">
                    <div class="buscar-filtros-header" onclick="toggleFiltros()">
                        <h3>⚙ Filtros</h3>
                        <span class="toggle-icon" id="filtrosToggleIcon">▼</span>
                    </div>
                    <div class="buscar-filtros-body" id="filtrosBody">

                        <!-- Filtro de completitud -->
                        <div class="filtro-seccion">
                            <h4>Estado de completitud</h4>
                            <div class="filtro-grupo">
                                <label><input type="radio" name="filtroCompleta" value="todas" checked> Todas</label>
                                <label><input type="radio" name="filtroCompleta" value="completas"> Completas</label>
                                <label><input type="radio" name="filtroCompleta" value="incompletas"> Incompletas</label>
                            </div>
                        </div>

                        <!-- Filtro de disponibilidad -->
                        <div class="filtro-seccion">
                            <h4>Disponibilidad (según frases activas)</h4>
                            <div class="filtro-grupo">
                                <label><input type="radio" name="filtroDisponibilidad" value="todas" checked> Todas</label>
                                <label><input type="radio" name="filtroDisponibilidad" value="disponibles"> Disponibles</label>
                                <label><input type="radio" name="filtroDisponibilidad" value="noDisponibles"> No disponibles</label>
                            </div>
                        </div>

                        <!-- Filtro de tipo de palabra -->
                        <div class="filtro-seccion">
                            <h4>Tipo de palabra</h4>
                            <div class="filtro-grupo">
                                <label><input type="checkbox" name="filtroTipo" value="verb" checked> Verbos</label>
                                <label><input type="checkbox" name="filtroTipo" value="noun" checked> Sustantivos</label>
                                <label><input type="checkbox" name="filtroTipo" value="adjective" checked> Adjetivos</label>
                                <label><input type="checkbox" name="filtroTipo" value="pronoun" checked> Pronombres</label>
                                <label><input type="checkbox" name="filtroTipo" value="numeral" checked> Numerales</label>
                                <label><input type="checkbox" name="filtroTipo" value="particle" checked> Partículas</label>
                            </div>
                        </div>

                        <!-- Filtros específicos de verbos -->
                        <div class="filtro-tipo-seccion" id="filtrosVerbo">
                            <h4>🔹 Filtros de Verbos</h4>
                            <div class="filtro-subseccion">
                                <div class="filtro-subseccion-titulo">Transitividad</div>
                                <div class="filtro-grupo">
                                    <label><input type="checkbox" name="filtroTransitividad" value="TRANSITIVO" checked> Transitivo</label>
                                    <label><input type="checkbox" name="filtroTransitividad" value="INTRANSITIVO" checked> Intransitivo</label>
                                    <label><input type="checkbox" name="filtroTransitividad" value="AMBITRANSITIVO" checked> Ambitransitivo</label>
                                    <label><input type="checkbox" name="filtroTransitividad" value="null" checked> Sin definir</label>
                                </div>
                            </div>
                            <div class="filtro-subseccion">
                                <div class="filtro-subseccion-titulo">Aspecto</div>
                                <div class="filtro-grupo">
                                    <label><input type="checkbox" name="filtroAspecto" value="PERFECTIVO" checked> Perfectivo</label>
                                    <label><input type="checkbox" name="filtroAspecto" value="IMPERFECTIVO" checked> Imperfectivo</label>
                                    <label><input type="checkbox" name="filtroAspecto" value="AMBIPREFECTIVO" checked> Biaspectual</label>
                                    <label><input type="checkbox" name="filtroAspecto" value="NULL" checked> Sin definir</label>
                                </div>
                            </div>
                        </div>

                        <!-- Filtros específicos de sustantivos -->
                        <div class="filtro-tipo-seccion" id="filtrosSustantivo">
                            <h4>🔸 Filtros de Sustantivos</h4>
                            <div class="filtro-subseccion">
                                <div class="filtro-subseccion-titulo">Género</div>
                                <div class="filtro-grupo">
                                    <label><input type="checkbox" name="filtroGenero" value="MASCULINO" checked> ♂️ Masculino</label>
                                    <label><input type="checkbox" name="filtroGenero" value="FEMENINO" checked> ♀️ Femenino</label>
                                    <label><input type="checkbox" name="filtroGenero" value="NEUTRO" checked> 👤 Neutro</label>
                                    <label><input type="checkbox" name="filtroGenero" value="null" checked> Sin definir</label>
                                </div>
                            </div>
                            <div class="filtro-subseccion">
                                <div class="filtro-subseccion-titulo">Animacidad</div>
                                <div class="filtro-grupo">
                                    <label><input type="radio" name="filtroAnimado" value="todos" checked> Todos</label>
                                    <label><input type="radio" name="filtroAnimado" value="ANIMADO"> Animado</label>
                                    <label><input type="radio" name="filtroAnimado" value="INANIMADO"> Inanimado</label>
                                    <label><input type="radio" name="filtroAnimado" value="null"> Sin definir</label>
                                </div>
                            </div>
                        </div>

                        <!-- Filtros específicos de pronombres -->
                        <div class="filtro-tipo-seccion" id="filtrosPronombre">
                            <h4>🔹 Filtros de Pronombres</h4>
                            <div class="filtro-subseccion">
                                <div class="filtro-subseccion-titulo">Tipo de pronombre</div>
                                <div class="filtro-grupo">
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="PERSONAL" checked> Personal</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="POSESIVO" checked> Posesivo</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="DEMOSTRATIVO" checked> Demostrativo</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="RELATIVO" checked> Relativo</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="INTERROGATIVO" checked> Interrogativo</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="REFLEXIVO" checked> Reflexivo</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="INDEFINIDO" checked> Indefinido</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="GENERALIZADOR" checked> Generalizador</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="NEGATIVO" checked> Negativo</label>
                                    <label><input type="checkbox" name="filtroTipoPronombre" value="null" checked> Sin definir</label>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>

                <!-- Info de resultados -->
                <div class="buscar-resultados-info" id="resultadosInfo">
                    <span id="contadorResultados">Escribe algo para buscar o pulsa Buscar para ver todas</span>
                </div>

                <!-- Spinner de carga -->
                <div class="spinner" id="spinnerBusqueda"></div>

                <!-- Lista de resultados -->
                <div class="buscar-resultados-lista" id="resultadosLista"></div>

            </div>

            <!-- ========== PANEL DERECHO: Detalle de flexiones ========== -->
            <div class="buscar-detalle-panel" id="detallePanel">
                <button class="detalle-cerrar" onclick="cerrarDetalle()">✕</button>

                <div class="detalle-cabecera" id="detalleCabecera"></div>

                <!-- Filtros de flexiones -->
                <div class="detalle-filtros">
                    <div class="detalle-filtro-grupo">
                        <span>Elegible:</span>
                        <label><input type="radio" name="filtroFlexElegible" value="todas" checked onchange="filtrarFlexiones()"> Todas</label>
                        <label><input type="radio" name="filtroFlexElegible" value="elegibles" onchange="filtrarFlexiones()"> Elegibles</label>
                        <label><input type="radio" name="filtroFlexElegible" value="noElegibles" onchange="filtrarFlexiones()"> No elegibles</label>
                    </div>
                    <div class="detalle-filtro-grupo">
                        <span>Estudio:</span>
                        <label><input type="radio" name="filtroFlexEstudio" value="todas" checked onchange="filtrarFlexiones()"> Todas</label>
                        <label><input type="radio" name="filtroFlexEstudio" value="estudiando" onchange="filtrarFlexiones()"> Estudiando</label>
                        <label><input type="radio" name="filtroFlexEstudio" value="noEstudiando" onchange="filtrarFlexiones()"> No estudiando</label>
                    </div>
                </div>

                <div class="flexion-counter" id="flexionCounter"></div>

                <!-- Tabla de flexiones -->
                <div class="flexion-table-container" id="flexionTableContainer"></div>
            </div>

        </div>
    </div>

    <script>
        // =====================================================
        // Estado global
        // =====================================================
        let todasLasPalabras = [];
        let palabrasFiltradas = [];
        let palabraSeleccionada = null;
        let flexionesActuales = [];
        let tipoPalabraSeleccionada = null;
        let debounceTimer = null;

        // =====================================================
        // Inicialización
        // =====================================================
        document.addEventListener('DOMContentLoaded', function() {
            // Buscar con Enter
            document.getElementById('searchInput').addEventListener('keypress', function(e) {
                if (e.key === 'Enter') buscar();
            });

            // Debounce al escribir
            document.getElementById('searchInput').addEventListener('input', function() {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(buscar, 400);
            });

            // Listeners de filtros
            document.querySelectorAll('[name="filtroCompleta"], [name="filtroDisponibilidad"], [name="filtroTipo"], [name="filtroTransitividad"], [name="filtroAspecto"], [name="filtroGenero"], [name="filtroAnimado"], [name="filtroTipoPronombre"]').forEach(el => {
                el.addEventListener('change', aplicarFiltrosLocales);
            });

            // Cargar todas las palabras al inicio
            buscar();
        });

        // =====================================================
        // Búsqueda
        // =====================================================
        function buscar() {
            const texto = document.getElementById('searchInput').value.trim();
            const spinner = document.getElementById('spinnerBusqueda');
            spinner.classList.add('spinner-active');

            fetch('/api/buscarPalabrasGuardadas?texto=' + encodeURIComponent(texto))
                .then(response => response.json())
                .then(data => {
                    spinner.classList.remove('spinner-active');
                    todasLasPalabras = data;
                    aplicarFiltrosLocales();
                })
                .catch(error => {
                    spinner.classList.remove('spinner-active');
                    console.error('Error:', error);
                    document.getElementById('contadorResultados').textContent = 'Error al buscar';
                });
        }

        // =====================================================
        // Filtros locales
        // =====================================================
        function aplicarFiltrosLocales() {
            const filtroCompleta = document.querySelector('[name="filtroCompleta"]:checked').value;
            const filtroDisponibilidad = document.querySelector('[name="filtroDisponibilidad"]:checked').value;
            const tiposActivos = [...document.querySelectorAll('[name="filtroTipo"]:checked')].map(el => el.value);

            const transitividadActiva = [...document.querySelectorAll('[name="filtroTransitividad"]:checked')].map(el => el.value);
            const aspectoActivo = [...document.querySelectorAll('[name="filtroAspecto"]:checked')].map(el => el.value);
            const generoActivo = [...document.querySelectorAll('[name="filtroGenero"]:checked')].map(el => el.value);
            const filtroAnimado = document.querySelector('[name="filtroAnimado"]:checked').value;
            const tipoPronombreActivo = [...document.querySelectorAll('[name="filtroTipoPronombre"]:checked')].map(el => el.value);

            palabrasFiltradas = todasLasPalabras.filter(p => {
                // Filtro de completitud
                if (filtroCompleta === 'completas' && !p.completa) return false;
                if (filtroCompleta === 'incompletas' && p.completa) return false;

                // Filtro de disponibilidad
                if (filtroDisponibilidad === 'disponibles' && !p.disponible) return false;
                if (filtroDisponibilidad === 'noDisponibles' && p.disponible) return false;

                // Filtro de tipo
                if (!tiposActivos.includes(p.tipo)) return false;

                // Filtros tipo-específicos
                if (p.tipo === 'verb') {
                    const trans = p.transitividad || 'null';
                    if (!transitividadActiva.includes(trans)) return false;
                    const asp = p.aspecto || 'NULL';
                    if (!aspectoActivo.includes(asp)) return false;
                }
                if (p.tipo === 'noun') {
                    const gen = p.genero || 'null';
                    if (!generoActivo.includes(gen)) return false;
                    const anim = p.animacidad || 'null';
                    if (filtroAnimado !== 'todos' && anim !== filtroAnimado) return false;
                }
                if (p.tipo === 'pronoun') {
                    const tp = p.tipoPronombre || 'null';
                    if (!tipoPronombreActivo.includes(tp)) return false;
                }

                return true;
            });

            renderizarResultados();
        }

        // =====================================================
        // Renderizado de resultados
        // =====================================================
        function renderizarResultados() {
            const lista = document.getElementById('resultadosLista');
            const contador = document.getElementById('contadorResultados');

            contador.textContent = palabrasFiltradas.length + ' resultado(s)' +
                (palabrasFiltradas.length !== todasLasPalabras.length ?
                    ' (de ' + todasLasPalabras.length + ' total)' : '');

            if (palabrasFiltradas.length === 0) {
                lista.innerHTML = '<div class="empty-state"><p>No se encontraron palabras</p></div>';
                return;
            }

            lista.innerHTML = palabrasFiltradas.map(p => {
                const selected = palabraSeleccionada && palabraSeleccionada.sloleksId === p.sloleksId ? ' selected' : '';
                const propBadges = obtenerPropBadges(p);
                return '<div class="buscar-resultado-item' + selected + '" onclick="seleccionarPalabra(\'' + p.sloleksId + '\', \'' + p.tipo + '\')">' +
                    '<span class="completa-badge ' + (p.completa ? 'si' : 'no') + '" title="' + (p.completa ? 'Completa' : 'Incompleta') + '"></span>' +
                    '<span class="buscar-resultado-principal">' + escapeHtml(p.principal) + '</span>' +
                    '<span class="buscar-resultado-significado">' + (p.significado ? escapeHtml(p.significado) : '<em>sin significado</em>') + '</span>' +
                    '<span class="buscar-resultado-tags">' +
                        propBadges +
                        '<span class="tipo-badge ' + p.tipo + '">' + escapeHtml(p.tipoEspanol) + '</span>' +
                        '<span class="prop-badge" title="Elegibles / Activas / Total">' + p.flexionesElegibles + '/' + p.flexionesActivas + '/' + p.totalFlexiones + '</span>' +
                    '</span>' +
                '</div>';
            }).join('');
        }

        function obtenerPropBadges(p) {
            let badges = '';
            if (p.tipo === 'verb') {
                if (p.transitividad) badges += '<span class="prop-badge">' + abreviarTransitividad(p.transitividad) + '</span>';
                if (p.aspecto) badges += '<span class="prop-badge">' + abreviarAspecto(p.aspecto) + '</span>';
                if (p.requiereSujetoAnimado) badges += '<span class="prop-badge" title="Requiere sujeto animado">S:' + p.requiereSujetoAnimado + '</span>';
                if (p.requiereObjetoAnimado) badges += '<span class="prop-badge" title="Requiere objeto animado">O:' + p.requiereObjetoAnimado + '</span>';
            }
            if (p.tipo === 'noun') {
                if (p.genero) badges += '<span class="prop-badge">' + abreviarGenero(p.genero) + '</span>';
                if (p.animacidad === 'ANIMADO') badges += '<span class="prop-badge">🐾 Anim.</span>';
                if (p.animacidad === 'INANIMADO') badges += '<span class="prop-badge">🪨 Inan.</span>';
                if (p.contabilidad) badges += '<span class="prop-badge">' + (p.contabilidad === 'CONTABLE' ? '🔢' : '💧') + '</span>';
                if (p.claseSemantica) badges += '<span class="prop-badge" title="Clase semántica">' + p.claseSemantica.substring(0,3) + '</span>';
            }
            if (p.tipo === 'pronoun' && p.tipoPronombre) {
                badges += '<span class="prop-badge">' + p.tipoPronombre.substring(0,4).toLowerCase() + '</span>';
            }
            if (p.disponible) {
                badges += '<span class="prop-badge" title="Disponible en frases activas">✅</span>';
            }
            return badges;
        }

        function abreviarTransitividad(t) {
            switch(t) {
                case 'TRANSITIVO': return 'Tr';
                case 'INTRANSITIVO': return 'In';
                case 'AMBITRANSITIVO': return 'Am';
                default: return t;
            }
        }

        function abreviarAspecto(a) {
            switch(a) {
                case 'PERFECTIVO': return '⏸️';
                case 'IMPERFECTIVO': return '▶️';
                case 'AMBIPREFECTIVO': return '⏯️';
                case 'NULL': return '❓';
                default: return a;
            }
        }

        function abreviarGenero(g) {
            switch(g) {
                case 'MASCULINO': return '♂️';
                case 'FEMENINO': return '♀️';
                case 'NEUTRO': return '👤';
                default: return g;
            }
        }

        // =====================================================
        // Selección de palabra y carga de detalle
        // =====================================================
        function seleccionarPalabra(sloleksId, tipo) {
            palabraSeleccionada = todasLasPalabras.find(p => p.sloleksId === sloleksId);
            tipoPalabraSeleccionada = tipo;

            // Marcar como seleccionado visualmente
            document.querySelectorAll('.buscar-resultado-item').forEach(el => el.classList.remove('selected'));
            if (event && event.target) {
                event.target.closest('.buscar-resultado-item').classList.add('selected');
            }

            // Cargar flexiones
            cargarDetalle(sloleksId, tipo);
        }

        function cargarDetalle(sloleksId, tipo) {
            const panel = document.getElementById('detallePanel');
            const cabecera = document.getElementById('detalleCabecera');
            const container = document.getElementById('flexionTableContainer');

            panel.classList.add('visible');
            container.innerHTML = '<div class="spinner spinner-active"></div>';

            // Renderizar cabecera
            const p = palabraSeleccionada;
            let propsHtml = '';
            if (p.tipo === 'verb') {
                if (p.transitividad) propsHtml += '<span class="detalle-prop-tag">' + p.transitividad + '</span>';
                if (p.aspecto) propsHtml += '<span class="detalle-prop-tag">' + p.aspecto + '</span>';
                if (p.verboOtroAspecto) propsHtml += '<span class="detalle-prop-tag">Par: ' + escapeHtml(p.verboOtroAspecto) + '</span>';
                if (p.requiereSujetoAnimado) propsHtml += '<span class="detalle-prop-tag">Suj.anim: ' + p.requiereSujetoAnimado + '</span>';
                if (p.requiereObjetoAnimado) propsHtml += '<span class="detalle-prop-tag">Obj.anim: ' + p.requiereObjetoAnimado + '</span>';
            }
            if (p.tipo === 'noun') {
                if (p.genero) propsHtml += '<span class="detalle-prop-tag">' + p.genero + '</span>';
                if (p.animacidad) propsHtml += '<span class="detalle-prop-tag">' + (p.animacidad === 'ANIMADO' ? 'Animado' : 'Inanimado') + '</span>';
                if (p.contabilidad) propsHtml += '<span class="detalle-prop-tag">' + (p.contabilidad === 'CONTABLE' ? 'Contable' : 'Incontable') + '</span>';
                if (p.claseSemantica) propsHtml += '<span class="detalle-prop-tag">' + p.claseSemantica + '</span>';
                if (p.cabezaRelacional) propsHtml += '<span class="detalle-prop-tag">Cab.rel: ' + p.cabezaRelacional + '</span>';
            }
            if (p.tipo === 'pronoun' && p.tipoPronombre) {
                propsHtml += '<span class="detalle-prop-tag">' + p.tipoPronombre + '</span>';
            }
            propsHtml += '<span class="detalle-prop-tag">' + p.flexionesElegibles + ' elegibles / ' + p.flexionesActivas + ' activas / ' + p.totalFlexiones + ' total</span>';

            cabecera.innerHTML =
                '<h2>' + escapeHtml(p.principal) + ' <span class="tipo-badge ' + p.tipo + '">' + escapeHtml(p.tipoEspanol) + '</span></h2>' +
                '<div class="detalle-significado">' + (p.significado ? escapeHtml(p.significado) : '<em>sin significado</em>') + '</div>' +
                '<div class="detalle-props">' + propsHtml + '</div>';

            // Reset filtros de flexiones
            document.querySelectorAll('[name="filtroFlexElegible"]')[0].checked = true;
            document.querySelectorAll('[name="filtroFlexEstudio"]')[0].checked = true;

            fetch('/api/detalleFlexiones?sloleksId=' + encodeURIComponent(sloleksId) + '&tipo=' + encodeURIComponent(tipo))
                .then(response => response.json())
                .then(data => {
                    flexionesActuales = data;
                    filtrarFlexiones();
                })
                .catch(error => {
                    console.error('Error:', error);
                    container.innerHTML = '<p>Error al cargar las flexiones</p>';
                });
        }

        // =====================================================
        // Filtrado y renderizado de flexiones
        // =====================================================
        function filtrarFlexiones() {
            const filtroElegible = document.querySelector('[name="filtroFlexElegible"]:checked').value;
            const filtroEstudio = document.querySelector('[name="filtroFlexEstudio"]:checked').value;

            let filtradas = flexionesActuales.filter(f => {
                if (filtroElegible === 'elegibles' && !f.elegible) return false;
                if (filtroElegible === 'noElegibles' && f.elegible) return false;
                if (filtroEstudio === 'estudiando' && !f.estudioIniciado) return false;
                if (filtroEstudio === 'noEstudiando' && f.estudioIniciado) return false;
                return true;
            });

            document.getElementById('flexionCounter').textContent =
                filtradas.length + ' flexión(es)' +
                (filtradas.length !== flexionesActuales.length ? ' (de ' + flexionesActuales.length + ' total)' : '');

            renderizarFlexiones(filtradas);
        }

        function renderizarFlexiones(flexiones) {
            const container = document.getElementById('flexionTableContainer');

            if (flexiones.length === 0) {
                container.innerHTML = '<div class="empty-state"><p>No hay flexiones con estos filtros</p></div>';
                return;
            }

            // Determinar columnas según tipo
            const columnas = obtenerColumnasParaTipo(tipoPalabraSeleccionada);

            let html = '<table class="flexion-table"><thead><tr>';
            html += '<th>Estado</th>';
            columnas.forEach(col => html += '<th>' + col.titulo + '</th>');
            html += '<th>Rev</th><th>Aciertos</th><th>%</th><th>Intervalo</th><th>Próx. Rev.</th>';
            html += '</tr></thead><tbody>';

            flexiones.forEach(f => {
                const clasesFila = [];
                if (!f.elegible) clasesFila.push('inactiva');
                if (f.enReaprendizaje) clasesFila.push('en-reaprendizaje');

                html += '<tr class="' + clasesFila.join(' ') + '">';

                // Estado
                html += '<td>';
                html += '<span class="flexion-estado-dot ' + (f.elegible ? 'activa' : 'inactiva') + '" title="' + (f.elegible ? 'Elegible' : 'No elegible') + '"></span>';
                if (f.estudioIniciado) html += '<span class="flexion-estado-dot estudiando" title="Estudiando"></span>';
                if (f.enReaprendizaje) html += '🔄';
                html += '</td>';

                // Columnas gramaticales
                columnas.forEach(col => {
                    html += '<td>' + (col.valor(f) || '-') + '</td>';
                });

                // Estadísticas SRS
                html += '<td>' + (f.totalRevisiones || 0) + '</td>';
                html += '<td>' + (f.totalAciertos || 0) + '</td>';
                html += '<td>' + (f.tasaAciertos != null ? f.tasaAciertos.toFixed(1) + '%' : '-') + '</td>';
                html += '<td>' + (f.intervaloLegible || '-') + '</td>';
                html += '<td>' + (f.proximaRevision || '-') + '</td>';
                html += '</tr>';
            });

            html += '</tbody></table>';
            container.innerHTML = html;
        }

        function obtenerColumnasParaTipo(tipo) {
            switch(tipo) {
                case 'verb':
                    return [
                        { titulo: 'Flexión', valor: f => escapeHtml(f.flexion || '') },
                        { titulo: 'Forma', valor: f => f.formaVerbal },
                        { titulo: 'Persona', valor: f => f.persona },
                        { titulo: 'Número', valor: f => f.numero },
                        { titulo: 'Género', valor: f => f.genero },
                        { titulo: 'Neg.', valor: f => f.negativo === true ? '✗' : (f.negativo === false ? '' : '-') }
                    ];
                case 'noun':
                    return [
                        { titulo: 'Flexión', valor: f => escapeHtml(f.flexion || '') },
                        { titulo: 'Número', valor: f => f.numero },
                        { titulo: 'Caso', valor: f => f.caso }
                    ];
                case 'adjective':
                    return [
                        { titulo: 'Flexión', valor: f => escapeHtml(f.flexion || '') },
                        { titulo: 'Género', valor: f => f.genero },
                        { titulo: 'Número', valor: f => f.numero },
                        { titulo: 'Caso', valor: f => f.caso },
                        { titulo: 'Grado', valor: f => f.grado },
                        { titulo: 'Definit.', valor: f => f.definitud }
                    ];
                case 'pronoun':
                    return [
                        { titulo: 'Flexión', valor: f => escapeHtml(f.flexion || '') },
                        { titulo: 'Género', valor: f => f.genero },
                        { titulo: 'Número', valor: f => f.numero },
                        { titulo: 'Caso', valor: f => f.caso }
                    ];
                case 'numeral':
                    return [
                        { titulo: 'Flexión', valor: f => escapeHtml(f.flexion || '') },
                        { titulo: 'Género', valor: f => f.genero },
                        { titulo: 'Número', valor: f => f.numero },
                        { titulo: 'Caso', valor: f => f.caso }
                    ];
                default:
                    return [
                        { titulo: 'Flexión', valor: f => escapeHtml(f.flexion || '') }
                    ];
            }
        }

        // =====================================================
        // Utilidades
        // =====================================================
        function toggleFiltros() {
            const body = document.getElementById('filtrosBody');
            const icon = document.getElementById('filtrosToggleIcon');
            body.classList.toggle('open');
            icon.textContent = body.classList.contains('open') ? '▲' : '▼';
        }

        function cerrarDetalle() {
            document.getElementById('detallePanel').classList.remove('visible');
            palabraSeleccionada = null;
            document.querySelectorAll('.buscar-resultado-item').forEach(el => el.classList.remove('selected'));
        }

        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.appendChild(document.createTextNode(text));
            return div.innerHTML;
        }
    </script>
</body>
</html>

