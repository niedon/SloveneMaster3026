<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcadaval.esloveno.beans.enums.TipoPalabra" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Completar Palabras - Esloveno</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <style>
        .search-container {
            margin-bottom: 30px;
        }

        .search-input {
            width: 100%;
            padding: 15px;
            font-size: 18px;
            border: 2px solid #667eea;
            border-radius: 10px;
            box-sizing: border-box;
        }

        .palabras-list {
            max-height: 400px;
            overflow-y: auto;
            border: 1px solid #ddd;
            border-radius: 10px;
            margin-bottom: 30px;
        }

        .palabra-item {
            padding: 15px 20px;
            border-bottom: 1px solid #f0f0f0;
            cursor: pointer;
            transition: background-color 0.3s ease;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .palabra-item:hover {
            background-color: #f8f9fa;
        }

        .palabra-item:last-child {
            border-bottom: none;
        }

        .palabra-item.selected {
            background-color: #e7f3ff;
            border-left: 4px solid #667eea;
        }

        .palabra-text {
            font-size: 18px;
            font-weight: 600;
            color: #333;
        }

        .palabra-tipo {
            font-size: 14px;
            color: #666;
            background-color: #f0f0f0;
            padding: 4px 12px;
            border-radius: 12px;
        }

        .form-container {
            display: none;
            background-color: #f8f9fa;
            padding: 30px;
            border-radius: 10px;
            margin-top: 20px;
        }

        .form-container.visible {
            display: block;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #333;
        }

        .form-group input[type="text"],
        .form-group select {
            width: 100%;
            padding: 12px;
            border: 2px solid #ddd;
            border-radius: 8px;
            font-size: 16px;
            box-sizing: border-box;
        }

        .form-group input[type="text"]:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
        }

        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .checkbox-group input[type="checkbox"] {
            width: 20px;
            height: 20px;
            cursor: pointer;
        }

        .form-actions {
            display: flex;
            gap: 15px;
            margin-top: 30px;
        }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #666;
        }

        .empty-state h3 {
            color: #333;
            margin-bottom: 10px;
        }

        .palabra-count {
            text-align: center;
            margin-bottom: 20px;
            font-size: 16px;
            color: #666;
        }
    </style>
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
            <div class="spinner" style="display: block;"></div>
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
                <div class="form-group" id="transitividadGroup" style="display: none;">
                    <label for="transitividad">Transitividad *</label>
                    <select id="transitividad" name="transitividad">
                        <option value="">Seleccione...</option>
                        <option value="TRANSITIVO">Transitivo</option>
                        <option value="INTRANSITIVO">Intransitivo</option>
                        <option value="AMBITRANSITIVO">Ambitransitivo</option>
                    </select>
                </div>

                <!-- Campo Animado (solo sustantivos) -->
                <div class="form-group checkbox-group" id="animadoGroup" style="display: none;">
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
                transitividadGroup.style.display = 'block';
                animadoGroup.style.display = 'none';
                transitividadSelect.value = palabra.transitividad || '';
                transitividadSelect.required = true;
            } else if (tipoEnum === ENUM_SUSTANTIVO) {
                transitividadGroup.style.display = 'none';
                animadoGroup.style.display = 'flex';
                transitividadSelect.required = false;
                animadoCheck.checked = palabra.animado === true;
            } else {
                // Para adjetivos, pronombres y numerales solo mostrar significado
                transitividadGroup.style.display = 'none';
                animadoGroup.style.display = 'none';
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
            messageBox.style.display = 'block';

            setTimeout(() => {
                messageBox.style.display = 'none';
            }, 5000);
        }
    </script>
</body>
</html>

