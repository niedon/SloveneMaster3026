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

        <!-- Buscador -->
        <div class="search-container">
            <input type="text"
                   class="search-input"
                   id="searchInput"
                   placeholder="🔍 Buscar palabra..."
                   autocomplete="off">
        </div>

        <!-- Lista de palabras incompletas -->
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

                <!-- Campo Animado (solo sustantivos) -->
                <div class="form-group checkbox-group hidden" id="animadoGroup">
                    <input type="checkbox" id="animado" name="animado">
                    <label for="animado">Es animado</label>
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
            }
        };

        // Cargar palabras al iniciar
        document.addEventListener('DOMContentLoaded', function() {
            cargarPalabras();
        });

        // Buscador en tiempo real
        document.getElementById('searchInput').addEventListener('input', function(e) {
            const query = e.target.value.toLowerCase().trim();
            if (query === '') {
                palabrasFiltradas = palabrasCompletas;
            } else {
                palabrasFiltradas = palabrasCompletas.filter(p =>
                    p.palabra.toLowerCase().includes(query)
                );
            }
            renderizarLista();
        });

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
                    palabrasFiltradas = data;
                    renderizarLista();
                    actualizarContador();
                })
                .catch(error => {
                    console.error('Error:', error);
                    mostrarMensaje('Error al cargar las palabras', 'error');
                });
        }

        function renderizarLista() {
            const lista = document.getElementById('palabrasList');

            if (palabrasFiltradas.length === 0) {
                lista.innerHTML = `
                    <div class="empty-state">
                        <h3>¡Excelente!</h3>
                        <p>No hay palabras incompletas</p>
                    </div>
                `;
                return;
            }

            lista.innerHTML = palabrasFiltradas.map(palabra => {
                const tipoInfo = TIPO_PALABRA[palabra.tipo];
                const nombreTipo = tipoInfo ? tipoInfo.nombre : palabra.tipo;
                return `
                    <div class="palabra-item" onclick="seleccionarPalabra('\${palabra.id}')">
                        <span class="palabra-text">\${palabra.palabra}</span>
                        <span class="palabra-tipo">\${nombreTipo}</span>
                    </div>
                `;
            }).join('');
        }

        function actualizarContador() {
            const contador = document.getElementById('palabraCount');
            contador.textContent = `\${palabrasCompletas.length} palabra(s) incompleta(s)`;
        }

        function seleccionarPalabra(id) {
            palabraSeleccionada = palabrasCompletas.find(p => p.id === id);
            if (!palabraSeleccionada) return;

            // Actualizar selección visual
            document.querySelectorAll('.palabra-item').forEach(item => {
                item.classList.remove('selected');
            });
            event.target.closest('.palabra-item').classList.add('selected');

            // Mostrar formulario
            mostrarFormulario(palabraSeleccionada);
        }

        function mostrarFormulario(palabra) {
            const formContainer = document.getElementById('formContainer');
            const formTitle = document.getElementById('formTitle');

            // Convertir el tipo XML al enum de TipoPalabra
            const tipoInfo = TIPO_PALABRA[palabra.tipo];
            const tipoEnum = tipoInfo ? tipoInfo.enum : palabra.tipo;

            // Llenar campos
            document.getElementById('palabraId').value = palabra.id;
            document.getElementById('palabraTipo').value = tipoEnum; // Usar el nombre del enum
            document.getElementById('significado').value = palabra.significado || '';

            formTitle.textContent = `Completar: \${palabra.palabra}`;

            // Mostrar/ocultar campos según tipo
            const transitividadGroup = document.getElementById('transitividadGroup');
            const animadoGroup = document.getElementById('animadoGroup');
            const transitividadSelect = document.getElementById('transitividad');
            const animadoCheck = document.getElementById('animado');

            if (tipoEnum === ENUM_VERBO) {
                transitividadGroup.classList.remove('hidden');
                animadoGroup.classList.add('hidden');
                transitividadSelect.value = palabra.transitividad || '';
                transitividadSelect.required = true;
            } else if (tipoEnum === ENUM_SUSTANTIVO) {
                transitividadGroup.classList.add('hidden');
                animadoGroup.classList.remove('hidden');
                transitividadSelect.required = false;
                animadoCheck.checked = palabra.animado === true;
            } else {
                // Para adjetivos, pronombres y numerales solo mostrar significado
                transitividadGroup.classList.add('hidden');
                animadoGroup.classList.add('hidden');
                transitividadSelect.required = false;
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

            // Validar que todos los campos requeridos estén llenos
            const significado = formData.get('significado').trim();
            if (!significado) {
                mostrarMensaje('El significado es obligatorio', 'error');
                return;
            }

            if (tipo === ENUM_VERBO && !formData.get('transitividad')) {
                mostrarMensaje('La transitividad es obligatoria para verbos', 'error');
                return;
            }

            // Convertir checkbox a boolean
            if (tipo === ENUM_SUSTANTIVO) {
                const animado = document.getElementById('animado').checked;
                formData.set('animado', animado);
            }

            // Enviar al servidor
            fetch('/api/actualizarPalabra', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.exito) {
                    mostrarMensaje('✅ ' + data.mensaje, 'success');
                    cancelarEdicion();
                    // Recargar lista
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

