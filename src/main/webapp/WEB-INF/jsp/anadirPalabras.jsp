<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Añadir Palabras - Esloveno</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <style>
        .results-container {
            margin-top: 20px;
        }
        .results-counter {
            font-size: 1.2em;
            margin-bottom: 15px;
            padding: 10px;
            background: #f0f0f0;
            border-radius: 5px;
        }
        .result-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px;
            margin: 10px 0;
            border-radius: 8px;
            border: 1px solid #ddd;
        }
        .result-item.supported {
            background: #e8f5e9;
            border-color: #4caf50;
        }
        .result-item.not-supported {
            background: #ffebee;
            border-color: #f44336;
        }
        .result-info {
            flex-grow: 1;
        }
        .result-lema {
            font-size: 1.3em;
            font-weight: bold;
        }
        .result-sloleks-id {
            font-size: 0.75em;
            color: #888;
            margin-top: 2px;
            font-family: monospace;
        }
        .result-tipo {
            color: #666;
            margin-left: 10px;
        }
        .result-tipo-badge {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 15px;
            font-size: 0.85em;
            margin-left: 10px;
        }
        .result-tipo-badge.supported {
            background: #4caf50;
            color: white;
        }
        .result-tipo-badge.not-supported {
            background: #f44336;
            color: white;
        }
        .btn-add {
            background: #4caf50;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1em;
        }
        .btn-add:hover {
            background: #45a049;
        }
        .btn-add:disabled {
            background: #ccc;
            cursor: not-allowed;
        }
        .no-results {
            text-align: center;
            padding: 30px;
            color: #666;
        }
        .filter-container {
            margin: 15px 0;
        }
        .filter-container select {
            padding: 8px 15px;
            font-size: 1em;
            border-radius: 5px;
            border: 1px solid #ddd;
        }
    </style>
</head>
<body>
    <c:set var="pageTitle" value="📚 Añadir Palabras" scope="request"/>
    <%@ include file="navbar.jsp" %>
    <div class="container">

        <div class="instructions">
            <h2>Instrucciones</h2>
            <ul>
                <li>Escribe una palabra en esloveno en el campo de búsqueda</li>
                <li>Opcionalmente, filtra por tipo de palabra</li>
                <li>Haz clic en "Buscar" para encontrar todas las coincidencias</li>
                <li>Selecciona la palabra que deseas añadir haciendo clic en "Añadir"</li>
                <li>Las palabras en rojo no están soportadas aún</li>
            </ul>
        </div>

        <div class="search-form">
            <input type="text"
                   id="wordInput"
                   placeholder="Escribe una palabra en esloveno..."
                   autocomplete="off">
            <div class="filter-container">
                <select id="tipoFilter">
                    <option value="">Todos los tipos</option>
                    <option value="noun">Sustantivo</option>
                    <option value="verb">Verbo</option>
                    <option value="adjective">Adjetivo</option>
                    <option value="pronoun">Pronombre</option>
                    <option value="numeral">Numeral</option>
                </select>
            </div>
            <button type="button" class="btn-primary" onclick="buscarPalabras()">
                🔍 Buscar
            </button>
        </div>

        <div class="status-container" id="statusContainer">
            <!-- Loading spinner -->
            <div class="loading-container" id="loadingContainer" style="display: none;">
                <div class="spinner"></div>
                <div class="loading-text">Buscando...</div>
            </div>

            <!-- Mensaje de error -->
            <div class="message" id="messageBox" style="display: none;"></div>

            <!-- Resultados -->
            <div class="results-container" id="resultsContainer" style="display: none;">
                <div class="results-counter" id="resultsCounter"></div>
                <div id="resultsList"></div>
            </div>
        </div>
    </div>

    <script>
        const wordInput = document.getElementById('wordInput');
        const tipoFilter = document.getElementById('tipoFilter');
        const loadingContainer = document.getElementById('loadingContainer');
        const messageBox = document.getElementById('messageBox');
        const resultsContainer = document.getElementById('resultsContainer');
        const resultsCounter = document.getElementById('resultsCounter');
        const resultsList = document.getElementById('resultsList');

        let currentSessionId = null;

        // Permitir buscar con Enter
        wordInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                buscarPalabras();
            }
        });

        function buscarPalabras() {
            const palabra = wordInput.value.trim();

            if (!palabra) {
                mostrarMensaje('Por favor, escribe una palabra', 'error');
                return;
            }

            // Limpiar resultados anteriores
            limpiarResultados();
            loadingContainer.style.display = 'block';

            fetch('/api/buscarTodas?word=' + encodeURIComponent(palabra))
                .then(response => response.json())
                .then(data => {
                    loadingContainer.style.display = 'none';
                    procesarResultados(data);
                })
                .catch(error => {
                    loadingContainer.style.display = 'none';
                    mostrarMensaje('Error al buscar: ' + error.message, 'error');
                });
        }

        function procesarResultados(data) {
            if (!data.exito) {
                mostrarMensaje(data.mensaje, 'error');
                return;
            }

            // Extraer sessionId de la respuesta
            const parts = data.palabra.split('|');
            currentSessionId = parts[1];

            // Filtrar por tipo si está seleccionado
            const filtroTipo = tipoFilter.value;
            let resultadosFiltrados = data.resultados;
            if (filtroTipo) {
                resultadosFiltrados = data.resultados.filter(r => r.tipo === filtroTipo);
            }

            if (resultadosFiltrados.length === 0) {
                mostrarMensaje('No se encontraron resultados' + (filtroTipo ? ' para el tipo seleccionado' : ''), 'info');
                return;
            }

            // Mostrar contador
            resultsCounter.innerHTML = '📊 Se encontraron <strong>' + resultadosFiltrados.length + '</strong> resultado(s)' +
                (filtroTipo ? ' (filtrado de ' + data.totalResultados + ' total)' : '');

            // Mostrar resultados
            resultsList.innerHTML = '';
            resultadosFiltrados.forEach(resultado => {
                const div = document.createElement('div');
                div.className = 'result-item ' + (resultado.soportado ? 'supported' : 'not-supported');

                const infoDiv = document.createElement('div');
                infoDiv.className = 'result-info';
                infoDiv.innerHTML = '<div><span class="result-lema">' + resultado.lema + '</span>' +
                    '<span class="result-tipo-badge ' + (resultado.soportado ? 'supported' : 'not-supported') + '">' +
                    resultado.tipoEspanol + '</span></div>' +
                    '<div class="result-sloleks-id">' + resultado.sloleksId + '</div>';

                div.appendChild(infoDiv);

                if (resultado.soportado) {
                    const btn = document.createElement('button');
                    btn.className = 'btn-add';
                    btn.innerHTML = '➕ Añadir';
                    btn.onclick = function() { guardarPalabra(resultado.indice, btn); };
                    div.appendChild(btn);
                } else {
                    const span = document.createElement('span');
                    span.style.color = '#999';
                    span.innerHTML = '❌ No soportado';
                    div.appendChild(span);
                }

                resultsList.appendChild(div);
            });

            resultsContainer.style.display = 'block';
        }

        function guardarPalabra(indice, button) {
            if (!currentSessionId) {
                mostrarMensaje('Sesión expirada. Por favor, busca de nuevo.', 'error');
                return;
            }

            button.disabled = true;
            button.innerHTML = '⏳ Guardando...';

            fetch('/api/guardarPalabra?sessionId=' + currentSessionId + '&indice=' + indice, {
                method: 'POST'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.exito) {
                        // Éxito - limpiar todo y mostrar mensaje
                        limpiarTodo();
                        mostrarMensaje('✅ ' + data.mensaje, 'success');
                    } else {
                        button.disabled = false;
                        button.innerHTML = '➕ Añadir';
                        mostrarMensaje('❌ ' + data.mensaje, 'error');
                    }
                })
                .catch(error => {
                    button.disabled = false;
                    button.innerHTML = '➕ Añadir';
                    mostrarMensaje('Error al guardar: ' + error.message, 'error');
                });
        }

        function limpiarResultados() {
            messageBox.style.display = 'none';
            resultsContainer.style.display = 'none';
            resultsList.innerHTML = '';
        }

        function limpiarTodo() {
            wordInput.value = '';
            tipoFilter.value = '';
            currentSessionId = null;
            limpiarResultados();
        }

        function mostrarMensaje(mensaje, tipo) {
            messageBox.innerHTML = mensaje;
            messageBox.className = 'message ' + tipo;
            messageBox.style.display = 'block';
        }
    </script>
</body>
</html>
