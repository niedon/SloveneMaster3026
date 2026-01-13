<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Configuración - Esloveno</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <style>
        .config-section {
            margin-bottom: 40px;
        }

        .config-section h2 {
            color: #667eea;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }

        .config-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
        }

        .config-item {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            border-left: 4px solid #667eea;
        }

        .config-item label {
            display: block;
            font-weight: 600;
            margin-bottom: 8px;
            color: #333;
        }

        .config-item input[type="number"],
        .config-item input[type="text"] {
            width: 100%;
            padding: 12px;
            border: 2px solid #ddd;
            border-radius: 8px;
            font-size: 16px;
            box-sizing: border-box;
        }

        .config-item input:focus {
            outline: none;
            border-color: #667eea;
        }

        .config-item .help-text {
            font-size: 12px;
            color: #6c757d;
            margin-top: 5px;
        }

        .casos-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 15px;
        }

        .caso-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s ease;
            border: 2px solid transparent;
        }

        .caso-item:hover {
            background: #e9ecef;
        }

        .caso-item.selected {
            background: #e7f3ff;
            border-color: #667eea;
        }

        .caso-item input[type="checkbox"] {
            width: 20px;
            height: 20px;
            cursor: pointer;
        }

        .caso-item label {
            cursor: pointer;
            font-weight: 500;
        }

        .nav-links {
            display: flex;
            gap: 15px;
            justify-content: center;
            margin-bottom: 30px;
        }

        .nav-links a {
            color: #667eea;
            text-decoration: none;
            padding: 10px 20px;
            border: 2px solid #667eea;
            border-radius: 8px;
            transition: all 0.3s ease;
        }

        .nav-links a:hover {
            background: #667eea;
            color: white;
        }

        .form-actions {
            display: flex;
            gap: 15px;
            justify-content: center;
            margin-top: 30px;
        }

        .tiempo-display {
            font-size: 12px;
            color: #667eea;
            margin-top: 5px;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <c:set var="pageTitle" value="⚙️ Configuración" scope="request"/>
    <%@ include file="navbar.jsp" %>
    <div class="container" style="max-width: 1000px;">


        <!-- Mensaje de resultado -->
        <div class="message" id="messageBox"></div>

        <form id="configForm">
            <!-- Sección de intervalos -->
            <div class="config-section">
                <h2>⏱️ Intervalos de Repetición</h2>
                <div class="config-grid">
                    <div class="config-item">
                        <label for="intervaloInicial">Primera Repetición (segundos)</label>
                        <input type="number" id="intervaloInicial" name="intervaloInicial"
                               value="${variablesMap['INTERVALO_INICIAL_SEGUNDOS'] != null ? variablesMap['INTERVALO_INICIAL_SEGUNDOS'].valor : '600'}"
                               min="10" step="10">
                        <div class="help-text">Tiempo antes de la primera repetición</div>
                        <div class="tiempo-display" id="tiempoInicial"></div>
                    </div>
                    <div class="config-item">
                        <label for="intervaloSegunda">Segunda Repetición (segundos)</label>
                        <input type="number" id="intervaloSegunda" name="intervaloSegunda"
                               value="${variablesMap['INTERVALO_SEGUNDA_SEGUNDOS'] != null ? variablesMap['INTERVALO_SEGUNDA_SEGUNDOS'].valor : '3600'}"
                               min="60" step="60">
                        <div class="help-text">Tiempo antes de la segunda repetición</div>
                        <div class="tiempo-display" id="tiempoSegunda"></div>
                    </div>
                    <div class="config-item">
                        <label for="intervaloReaprendizaje">Reaprendizaje (segundos)</label>
                        <input type="number" id="intervaloReaprendizaje" name="intervaloReaprendizaje"
                               value="${variablesMap['INTERVALO_REAPRENDIZAJE_SEGUNDOS'] != null ? variablesMap['INTERVALO_REAPRENDIZAJE_SEGUNDOS'].valor : '30'}"
                               min="5" step="5">
                        <div class="help-text">Tiempo tras fallar una tarjeta</div>
                        <div class="tiempo-display" id="tiempoReaprendizaje"></div>
                    </div>
                </div>
            </div>

            <!-- Sección de factores -->
            <div class="config-section">
                <h2>📈 Factores de Dificultad</h2>
                <div class="config-grid">
                    <div class="config-item">
                        <label for="factorFacilidadInicial">Factor Inicial</label>
                        <input type="number" id="factorFacilidadInicial" name="factorFacilidadInicial"
                               value="${variablesMap['FACTOR_FACILIDAD_INICIAL'] != null ? variablesMap['FACTOR_FACILIDAD_INICIAL'].valor : '2.5'}"
                               min="1.0" max="5.0" step="0.1">
                        <div class="help-text">Factor de facilidad inicial (2.5 recomendado)</div>
                    </div>
                    <div class="config-item">
                        <label for="factorFacilidadMinimo">Factor Mínimo</label>
                        <input type="number" id="factorFacilidadMinimo" name="factorFacilidadMinimo"
                               value="${variablesMap['FACTOR_FACILIDAD_MINIMO'] != null ? variablesMap['FACTOR_FACILIDAD_MINIMO'].valor : '1.3'}"
                               min="1.0" max="3.0" step="0.1">
                        <div class="help-text">Factor mínimo (tarjetas más difíciles)</div>
                    </div>
                    <div class="config-item">
                        <label for="penalizacionFallo">Penalización por Fallo</label>
                        <input type="number" id="penalizacionFallo" name="penalizacionFallo"
                               value="${variablesMap['PENALIZACION_FALLO'] != null ? variablesMap['PENALIZACION_FALLO'].valor : '0.2'}"
                               min="0.05" max="0.5" step="0.05">
                        <div class="help-text">Cuánto se reduce el factor al fallar</div>
                    </div>
                </div>
            </div>

            <!-- Sección de límites -->
            <div class="config-section">
                <h2>🎯 Límites Diarios</h2>
                <div class="config-grid">
                    <div class="config-item">
                        <label for="maxTarjetasNuevas">Máximo Tarjetas Nuevas/Día</label>
                        <input type="number" id="maxTarjetasNuevas" name="maxTarjetasNuevas"
                               value="${variablesMap['MAX_TARJETAS_NUEVAS_DIA'] != null ? variablesMap['MAX_TARJETAS_NUEVAS_DIA'].valor : '20'}"
                               min="1" max="100" step="1">
                        <div class="help-text">Límite de tarjetas nuevas por día</div>
                    </div>
                    <div class="config-item">
                        <label for="maxTarjetasRevision">Máximo Revisiones/Día</label>
                        <input type="number" id="maxTarjetasRevision" name="maxTarjetasRevision"
                               value="${variablesMap['MAX_TARJETAS_REVISION_DIA'] != null ? variablesMap['MAX_TARJETAS_REVISION_DIA'].valor : '100'}"
                               min="1" max="500">
                        <div class="help-text">Límite de revisiones por día</div>
                    </div>
                </div>
            </div>

            <!-- Sección de estructuras de frase -->
            <div class="config-section">
                <h2>📝 Estructuras de Frase</h2>
                <p>Activa o desactiva las estructuras de frase que deseas estudiar.</p>
                <div class="casos-grid">
                    <c:forEach var="estructura" items="${estructuras}">
                        <div class="caso-item <c:if test='${estructura.activa}'>selected</c:if>"
                             onclick="toggleEstructuraLocal('${estructura.identificador}', this)"
                             data-identificador="${estructura.identificador}"
                             data-casos="<c:forEach var='caso' items='${estructura.casosUsados}' varStatus='st'>${caso.name()}<c:if test='${!st.last}'>,</c:if></c:forEach>"
                             data-formas-verbales="<c:forEach var='fv' items='${estructura.formasVerbalesUsadas}' varStatus='st'>${fv.name()}<c:if test='${!st.last}'>,</c:if></c:forEach>">
                            <input type="checkbox" id="est_${estructura.identificador}"
                                   name="estructuras"
                                   value="${estructura.identificador}"
                                   <c:if test="${estructura.activa}">checked</c:if>>
                            <label for="est_${estructura.identificador}">${estructura.nombreMostrar}</label>
                        </div>
                    </c:forEach>
                </div>
                <div style="margin-top: 15px; padding: 15px; background: #e7f3ff; border-radius: 10px;">
                    <div style="margin-bottom: 10px;">
                        <strong>📌 Casos activos:</strong> <span id="casosActivosDisplay">
                        <c:forEach var="caso" items="${casosActivos}" varStatus="status">
                            ${caso.name()}<c:if test="${!status.last}">, </c:if>
                        </c:forEach>
                        <c:if test="${empty casosActivos}">Ninguno</c:if>
                        </span>
                    </div>
                    <div>
                        <strong>🔤 Formas verbales activas:</strong> <span id="formasVerbalesActivasDisplay">
                        <c:forEach var="fv" items="${formasVerbalesActivas}" varStatus="status">
                            ${fv.name()}<c:if test="${!status.last}">, </c:if>
                        </c:forEach>
                        <c:if test="${empty formasVerbalesActivas}">Ninguna</c:if>
                        </span>
                    </div>
                    <div class="help-text">Los casos y formas verbales se determinan automáticamente según las estructuras activas</div>
                </div>
            </div>

            <div class="form-actions">
                <button type="button" class="btn-secondary" onclick="location.reload()">Cancelar</button>
                <button type="submit" class="btn-primary">💾 Guardar Configuración</button>
            </div>
        </form>
    </div>

    <script>
        // Función para formatear segundos a formato legible
        function formatearTiempo(segundos) {
            if (segundos < 60) return segundos + ' segundos';
            if (segundos < 3600) return Math.floor(segundos / 60) + ' minutos';
            if (segundos < 86400) {
                const horas = Math.floor(segundos / 3600);
                const mins = Math.floor((segundos % 3600) / 60);
                return horas + 'h ' + (mins > 0 ? mins + 'm' : '');
            }
            const dias = Math.floor(segundos / 86400);
            const horas = Math.floor((segundos % 86400) / 3600);
            return dias + ' día(s) ' + (horas > 0 ? horas + 'h' : '');
        }

        // Actualizar displays de tiempo
        function actualizarTiempos() {
            const inicial = document.getElementById('intervaloInicial').value;
            const segunda = document.getElementById('intervaloSegunda').value;
            const reaprendizaje = document.getElementById('intervaloReaprendizaje').value;

            document.getElementById('tiempoInicial').textContent = '≈ ' + formatearTiempo(parseInt(inicial));
            document.getElementById('tiempoSegunda').textContent = '≈ ' + formatearTiempo(parseInt(segunda));
            document.getElementById('tiempoReaprendizaje').textContent = '≈ ' + formatearTiempo(parseInt(reaprendizaje));
        }

        // Eventos para actualizar tiempos
        document.getElementById('intervaloInicial').addEventListener('input', actualizarTiempos);
        document.getElementById('intervaloSegunda').addEventListener('input', actualizarTiempos);
        document.getElementById('intervaloReaprendizaje').addEventListener('input', actualizarTiempos);
        actualizarTiempos();

        // Toggle de estructura de frase (LOCAL, no guarda hasta Submit)
        function toggleEstructuraLocal(identificador, element) {
            const checkbox = element.querySelector('input[type="checkbox"]');
            checkbox.checked = !checkbox.checked;
            element.classList.toggle('selected', checkbox.checked);

            // Actualizar casos y formas verbales activas dinámicamente
            actualizarFiltrosActivos();
        }

        // Calcula y actualiza la lista de casos y formas verbales activas según las estructuras seleccionadas
        function actualizarFiltrosActivos() {
            const estructurasSeleccionadas = document.querySelectorAll('.caso-item input[type="checkbox"]:checked');
            const casosSet = new Set();
            const formasVerbalesSet = new Set();

            estructurasSeleccionadas.forEach(checkbox => {
                const casoItem = checkbox.closest('.caso-item');

                // Obtener casos
                const casos = casoItem.getAttribute('data-casos');
                if (casos && casos.trim() !== '') {
                    casos.split(',').forEach(caso => casosSet.add(caso.trim()));
                }

                // Obtener formas verbales
                const formasVerbales = casoItem.getAttribute('data-formas-verbales');
                if (formasVerbales && formasVerbales.trim() !== '') {
                    formasVerbales.split(',').forEach(fv => formasVerbalesSet.add(fv.trim()));
                }
            });

            // Actualizar display de casos
            const casosArray = Array.from(casosSet).sort();
            const casosDisplayElement = document.getElementById('casosActivosDisplay');
            casosDisplayElement.textContent = casosArray.length === 0 ? 'Ninguno' : casosArray.join(', ');

            // Actualizar display de formas verbales
            const formasVerbalesArray = Array.from(formasVerbalesSet).sort();
            const formasVerbalesDisplayElement = document.getElementById('formasVerbalesActivasDisplay');
            formasVerbalesDisplayElement.textContent = formasVerbalesArray.length === 0 ? 'Ninguna' : formasVerbalesArray.join(', ');
        }

        // Enviar formulario (variables + estructuras)
        document.getElementById('configForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const formData = new FormData();
            formData.append('intervaloInicial', document.getElementById('intervaloInicial').value);
            formData.append('intervaloSegunda', document.getElementById('intervaloSegunda').value);
            formData.append('intervaloReaprendizaje', document.getElementById('intervaloReaprendizaje').value);
            formData.append('factorFacilidadInicial', document.getElementById('factorFacilidadInicial').value);
            formData.append('factorFacilidadMinimo', document.getElementById('factorFacilidadMinimo').value);
            formData.append('penalizacionFallo', document.getElementById('penalizacionFallo').value);
            formData.append('maxTarjetasNuevas', document.getElementById('maxTarjetasNuevas').value);
            formData.append('maxTarjetasRevision', document.getElementById('maxTarjetasRevision').value);

            // Guardar configuración de variables
            fetch('/api/guardarConfiguracion', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.exito) {
                    // Ahora guardar las estructuras
                    return guardarEstructuras();
                } else {
                    throw new Error(data.mensaje);
                }
            })
            .then(() => {
                mostrarMensaje('✅ Configuración guardada correctamente', 'success');
            })
            .catch(error => {
                mostrarMensaje('❌ Error al guardar: ' + error.message, 'error');
            });
        });

        // Guarda el estado de todas las estructuras
        function guardarEstructuras() {
            const checkboxes = document.querySelectorAll('input[name="estructuras"]');
            const promises = [];

            checkboxes.forEach(checkbox => {
                const identificador = checkbox.value;
                const activa = checkbox.checked;

                const formData = new FormData();
                formData.append('identificador', identificador);
                formData.append('activa', activa);

                promises.push(
                    fetch('/api/toggleEstructura', {
                        method: 'POST',
                        body: formData
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (!data.exito) {
                            throw new Error('Error en estructura ' + identificador);
                        }
                    })
                );
            });

            return Promise.all(promises);
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

