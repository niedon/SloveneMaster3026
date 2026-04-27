<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcadaval.esloveno.beans.enums.TipoPalabra" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Completar Palabras - Esloveno</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css?v=${cssVersion}">
    <%@ include file="head-favicon.jsp" %>
</head>
<body>
    <c:set var="pageTitle" value="📝 Completar Palabras" scope="request"/>
    <%@ include file="navbar.jsp" %>
    <div class="container">

        <!-- Mensaje de resultado -->
        <div class="message" id="messageBox"></div>

        <!-- Contador de palabras -->
        <div class="palabra-count" id="palabraCount"></div>

        <!-- Filtros -->
        <div class="search-container">
            <input type="text"
                   class="search-input"
                   id="searchInput"
                   placeholder="🔍 Buscar palabra..."
                   autocomplete="off">
            <div style="margin-top: 8px; display: flex; align-items: center; gap: 16px; flex-wrap: wrap;">
                <label style="display: flex; align-items: center; gap: 4px; cursor: pointer;">
                    <input type="checkbox" id="filtroIncompletas" checked>
                    Solo incompletas
                </label>
                <select id="filtroTipo" style="padding: 4px 8px; border-radius: 4px; border: 1px solid #ccc;">
                    <option value="">Todos los tipos</option>
                    <option value="<%= TipoPalabra.SUSTANTIVO.getXmlCode() %>">Sustantivos</option>
                    <option value="<%= TipoPalabra.VERBO.getXmlCode() %>">Verbos</option>
                    <option value="<%= TipoPalabra.ADJETIVO.getXmlCode() %>">Adjetivos</option>
                    <option value="<%= TipoPalabra.PRONOMBRE.getXmlCode() %>">Pronombres</option>
                    <option value="<%= TipoPalabra.NUMERAL.getXmlCode() %>">Numerales</option>
                    <option value="<%= TipoPalabra.PARTICULA.getXmlCode() %>">Partículas</option>
                </select>
            </div>
        </div>

        <!-- Lista de palabras -->
        <div class="palabras-list" id="palabrasList">
            <div class="spinner spinner-active"></div>
        </div>

        <!-- Formulario de edición -->
        <div class="form-container" id="formContainer">
            <h2 id="formTitle">Completar Palabra</h2>
            <form id="editForm">
                <input type="hidden" id="palabraId" name="id">
                <input type="hidden" id="palabraTipo" name="tipo">

                <!-- Campo Significado (para todos) -->
                <div class="form-group">
                    <label for="significado">Significado en español *</label>
                    <input type="text" id="significado" name="significado" required>
                </div>

                <!-- Campo Transitividad (solo verbos) -->
                <div class="form-group hidden" id="transitividadGroup">
                    <label for="transitividad">Transitividad *</label>
                    <select id="transitividad" name="transitividad">
                        <option value="">Seleccione...</option>
                        <option value="TRANSITIVO">Transitivo</option>
                        <option value="INTRANSITIVO">Intransitivo</option>
                        <option value="AMBITRANSITIVO">Ambitransitivo</option>
                    </select>
                </div>

                <!-- Campo RequiereSujetoAnimado (solo verbos) -->
                <div class="form-group hidden" id="requiereSujetoAnimadoGroup">
                    <label for="requiereSujetoAnimado">¿Requiere sujeto animado? *</label>
                    <select id="requiereSujetoAnimado" name="requiereSujetoAnimado">
                        <option value="">Seleccione...</option>
                        <option value="SI">Sí</option>
                        <option value="NO">No</option>
                    </select>
                </div>

                <!-- Campo RequiereObjetoAnimado (solo verbos) -->
                <div class="form-group hidden" id="requiereObjetoAnimadoGroup">
                    <label for="requiereObjetoAnimado">¿Requiere objeto animado? *</label>
                    <select id="requiereObjetoAnimado" name="requiereObjetoAnimado">
                        <option value="">Seleccione...</option>
                        <option value="SI">Sí</option>
                        <option value="NO">No</option>
                    </select>
                </div>

                <!-- Campo Animacidad (solo sustantivos) -->
                <div class="form-group hidden" id="animacidadGroup">
                    <label for="animacidad">Animacidad *</label>
                    <select id="animacidad" name="animacidad">
                        <option value="">Seleccione...</option>
                        <option value="ANIMADO">Animado</option>
                        <option value="INANIMADO">Inanimado</option>
                    </select>
                </div>

                <!-- Campo Contabilidad (solo sustantivos) -->
                <div class="form-group hidden" id="contabilidadGroup">
                    <label for="contabilidad">Contabilidad *</label>
                    <select id="contabilidad" name="contabilidad">
                        <option value="">Seleccione...</option>
                        <option value="CONTABLE">Contable</option>
                        <option value="INCONTABLE">Incontable</option>
                    </select>
                </div>

                <!-- Campo ClaseSemantica (solo sustantivos) -->
                <div class="form-group hidden" id="claseSemanticaGroup">
                    <label for="claseSemantica">Clase semántica *</label>
                    <select id="claseSemantica" name="claseSemantica">
                        <option value="">Seleccione...</option>
                        <option value="HUMANO">Humano</option>
                        <option value="ANIMAL">Animal</option>
                        <option value="OBJETO">Objeto</option>
                        <option value="LUGAR">Lugar</option>
                        <option value="SUSTANCIA">Sustancia</option>
                        <option value="ABSTRACTO">Abstracto</option>
                    </select>
                </div>

                <!-- Campo Cantidad (solo numerales) -->
                <div class="form-group hidden" id="cantidadGroup">
                    <label for="cantidad">Cantidad (valor numérico) *</label>
                    <input type="number" id="cantidad" name="cantidad" min="0" step="1"
                           placeholder="Ej: 1 para en, 2 para dva, 5 para pet...">
                </div>

                <div class="form-actions">
                    <button type="button" class="btn-secondary" onclick="cancelarEdicion()">Cancelar</button>
                    <button type="submit" class="btn-primary">Guardar Cambios</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        let palabrasCompletas = [];
        let palabrasFiltradas = [];
        let palabraSeleccionada = null;

        // Constantes del enum TipoPalabra desde Java
        const ENUM_SUSTANTIVO = '<%= TipoPalabra.SUSTANTIVO.name() %>';
        const ENUM_VERBO = '<%= TipoPalabra.VERBO.name() %>';
        const ENUM_ADJETIVO = '<%= TipoPalabra.ADJETIVO.name() %>';
        const ENUM_PRONOMBRE = '<%= TipoPalabra.PRONOMBRE.name() %>';
        const ENUM_NUMERAL = '<%= TipoPalabra.NUMERAL.name() %>';
        const ENUM_PARTICULA = '<%= TipoPalabra.PARTICULA.name() %>';

        // Valores del enum TipoPalabra desde Java
        const TIPO_PALABRA = {
            '<%= TipoPalabra.SUSTANTIVO.getXmlCode() %>': {
                enum: ENUM_SUSTANTIVO,
                nombre: '<%= TipoPalabra.SUSTANTIVO.getNombreEspanol() %>'
            },
            '<%= TipoPalabra.VERBO.getXmlCode() %>': {
                enum: ENUM_VERBO,
                nombre: '<%= TipoPalabra.VERBO.getNombreEspanol() %>'
            },
            '<%= TipoPalabra.ADJETIVO.getXmlCode() %>': {
                enum: ENUM_ADJETIVO,
                nombre: '<%= TipoPalabra.ADJETIVO.getNombreEspanol() %>'
            },
            '<%= TipoPalabra.PRONOMBRE.getXmlCode() %>': {
                enum: ENUM_PRONOMBRE,
                nombre: '<%= TipoPalabra.PRONOMBRE.getNombreEspanol() %>'
            },
            '<%= TipoPalabra.NUMERAL.getXmlCode() %>': {
                enum: ENUM_NUMERAL,
                nombre: '<%= TipoPalabra.NUMERAL.getNombreEspanol() %>'
            },
            '<%= TipoPalabra.PARTICULA.getXmlCode() %>': {
                enum: ENUM_PARTICULA,
                nombre: '<%= TipoPalabra.PARTICULA.getNombreEspanol() %>'
            }
        };

        // Cargar palabras al iniciar
        document.addEventListener('DOMContentLoaded', function() {
            cargarPalabras();
        });

        // Filtros en tiempo real
        document.getElementById('searchInput').addEventListener('input', aplicarFiltros);
        document.getElementById('filtroIncompletas').addEventListener('change', aplicarFiltros);
        document.getElementById('filtroTipo').addEventListener('change', aplicarFiltros);

        // Submit del formulario
        document.getElementById('editForm').addEventListener('submit', function(e) {
            e.preventDefault();
            guardarPalabra();
        });

        function cargarPalabras() {
            fetch('/api/palabrasIncompletas')
                .then(response => response.json())
                .then(data => {
                    palabrasCompletas = data;
                    aplicarFiltros();
                })
                .catch(error => {
                    console.error('Error:', error);
                    mostrarMensaje('Error al cargar las palabras', 'error');
                });
        }

        function aplicarFiltros() {
            const query = document.getElementById('searchInput').value.toLowerCase().trim();
            const soloIncompletas = document.getElementById('filtroIncompletas').checked;
            const filtroTipo = document.getElementById('filtroTipo').value;

            palabrasFiltradas = palabrasCompletas.filter(p => {
                if (query && !p.palabra.toLowerCase().includes(query)) return false;
                if (soloIncompletas && p.completa) return false;
                if (filtroTipo && p.tipo !== filtroTipo) return false;
                return true;
            });

            renderizarLista();
            actualizarContador();
        }

        function renderizarLista() {
            const lista = document.getElementById('palabrasList');

            if (palabrasFiltradas.length === 0) {
                const soloIncompletas = document.getElementById('filtroIncompletas').checked;
                lista.innerHTML = '<div class="empty-state"><h3>' +
                    (soloIncompletas ? '¡Excelente!' : 'Sin resultados') +
                    '</h3><p>' +
                    (soloIncompletas ? 'No hay palabras incompletas' : 'No se encontraron palabras') +
                    '</p></div>';
                return;
            }

            lista.innerHTML = palabrasFiltradas.map(palabra => {
                const tipoInfo = TIPO_PALABRA[palabra.tipo];
                const nombreTipo = tipoInfo ? tipoInfo.nombre : palabra.tipo;
                const estadoIcono = palabra.completa
                    ? '<span style="color:#28a745;font-size:0.8em;">✓</span>'
                    : '<span style="color:#dc3545;font-size:0.8em;">✗</span>';
                return '<div class="palabra-item" onclick="seleccionarPalabra(\'' + palabra.id + '\')">' +
                    '<span class="palabra-text">' + palabra.palabra + '</span>' +
                    '<span style="display:flex;gap:8px;align-items:center;">' +
                    estadoIcono +
                    '<span class="palabra-tipo">' + nombreTipo + '</span>' +
                    '</span></div>';
            }).join('');
        }

        function actualizarContador() {
            const contador = document.getElementById('palabraCount');
            const totalIncompletas = palabrasCompletas.filter(p => !p.completa).length;
            contador.textContent = palabrasFiltradas.length + ' palabra(s) mostrada(s) · ' + totalIncompletas + ' incompleta(s) en total';
        }

        function seleccionarPalabra(id) {
            palabraSeleccionada = palabrasCompletas.find(p => p.id === id);
            if (!palabraSeleccionada) return;

            document.querySelectorAll('.palabra-item').forEach(item => {
                item.classList.remove('selected');
            });
            event.target.closest('.palabra-item').classList.add('selected');

            mostrarFormulario(palabraSeleccionada);
        }

        function mostrarFormulario(palabra) {
            const formContainer = document.getElementById('formContainer');
            const formTitle = document.getElementById('formTitle');

            const tipoInfo = TIPO_PALABRA[palabra.tipo];
            const tipoEnum = tipoInfo ? tipoInfo.enum : palabra.tipo;

            document.getElementById('palabraId').value = palabra.id;
            document.getElementById('palabraTipo').value = tipoEnum;
            document.getElementById('significado').value = palabra.significado || '';

            formTitle.textContent = (palabra.completa ? 'Editar: ' : 'Completar: ') + palabra.palabra;

            // Ocultar todos los campos específicos
            var gruposEspecificos = [
                'transitividadGroup', 'requiereSujetoAnimadoGroup', 'requiereObjetoAnimadoGroup',
                'animacidadGroup', 'contabilidadGroup', 'claseSemanticaGroup', 'cantidadGroup'
            ];
            gruposEspecificos.forEach(function(g) { document.getElementById(g).classList.add('hidden'); });

            // Resetear required
            ['transitividad', 'requiereSujetoAnimado', 'requiereObjetoAnimado',
             'animacidad', 'contabilidad', 'claseSemantica'].forEach(function(id) {
                document.getElementById(id).required = false;
            });
            document.getElementById('cantidad').required = false;

            if (tipoEnum === ENUM_VERBO) {
                ['transitividadGroup', 'requiereSujetoAnimadoGroup', 'requiereObjetoAnimadoGroup'].forEach(function(g) {
                    document.getElementById(g).classList.remove('hidden');
                });
                document.getElementById('transitividad').value = palabra.transitividad || '';
                document.getElementById('transitividad').required = true;
                document.getElementById('requiereSujetoAnimado').value = palabra.requiereSujetoAnimado || '';
                document.getElementById('requiereSujetoAnimado').required = true;
                document.getElementById('requiereObjetoAnimado').value = palabra.requiereObjetoAnimado || '';
                document.getElementById('requiereObjetoAnimado').required = true;
            } else if (tipoEnum === ENUM_SUSTANTIVO) {
                ['animacidadGroup', 'contabilidadGroup', 'claseSemanticaGroup'].forEach(function(g) {
                    document.getElementById(g).classList.remove('hidden');
                });
                document.getElementById('animacidad').value = palabra.animacidad || '';
                document.getElementById('animacidad').required = true;
                document.getElementById('contabilidad').value = palabra.contabilidad || '';
                document.getElementById('contabilidad').required = true;
                document.getElementById('claseSemantica').value = palabra.claseSemantica || '';
                document.getElementById('claseSemantica').required = true;
            } else if (tipoEnum === ENUM_NUMERAL) {
                document.getElementById('cantidadGroup').classList.remove('hidden');
                document.getElementById('cantidad').value = palabra.cantidad != null ? palabra.cantidad : '';
                document.getElementById('cantidad').required = true;
            }

            formContainer.classList.add('visible');
            formContainer.scrollIntoView({ behavior: 'smooth' });
        }

        function cancelarEdicion() {
            document.getElementById('formContainer').classList.remove('visible');
            document.querySelectorAll('.palabra-item').forEach(item => {
                item.classList.remove('selected');
            });
            palabraSeleccionada = null;
        }

        function guardarPalabra() {
            const formData = new FormData(document.getElementById('editForm'));
            const tipo = formData.get('tipo');

            const significado = formData.get('significado').trim();
            if (!significado) {
                mostrarMensaje('El significado es obligatorio', 'error');
                return;
            }

            if (tipo === ENUM_VERBO) {
                if (!formData.get('transitividad')) { mostrarMensaje('La transitividad es obligatoria para verbos', 'error'); return; }
                if (!formData.get('requiereSujetoAnimado')) { mostrarMensaje('Requiere sujeto animado es obligatorio para verbos', 'error'); return; }
                if (!formData.get('requiereObjetoAnimado')) { mostrarMensaje('Requiere objeto animado es obligatorio para verbos', 'error'); return; }
            }

            if (tipo === ENUM_SUSTANTIVO) {
                if (!formData.get('animacidad')) { mostrarMensaje('La animacidad es obligatoria para sustantivos', 'error'); return; }
                if (!formData.get('contabilidad')) { mostrarMensaje('La contabilidad es obligatoria para sustantivos', 'error'); return; }
                if (!formData.get('claseSemantica')) { mostrarMensaje('La clase semántica es obligatoria para sustantivos', 'error'); return; }
            }

            if (tipo === ENUM_NUMERAL) {
                const cantidadVal = formData.get('cantidad');
                if (!cantidadVal || cantidadVal.trim() === '') { mostrarMensaje('La cantidad es obligatoria para numerales', 'error'); return; }
            }

            fetch('/api/actualizarPalabra', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.exito) {
                    mostrarMensaje('✅ ' + data.mensaje, 'success');
                    cancelarEdicion();
                    cargarPalabras();
                } else {
                    mostrarMensaje('❌ ' + data.mensaje, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                mostrarMensaje('Error al guardar la palabra', 'error');
            });
        }

        function mostrarMensaje(texto, tipo) {
            const messageBox = document.getElementById('messageBox');
            messageBox.textContent = texto;
            messageBox.className = 'message ' + tipo;

            setTimeout(() => {
                messageBox.classList.add('hidden');
            }, 5000);
        }
    </script>
</body>
</html>

