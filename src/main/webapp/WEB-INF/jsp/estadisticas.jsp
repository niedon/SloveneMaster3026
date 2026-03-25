<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Estadísticas de Estudio</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css?v=${cssVersion}">
    <%@ include file="head-favicon.jsp" %>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <style>
        /* Estilos específicos para la página de estadísticas */
        .control-panel .btn-group .btn {
            border-radius: 0;
        }
        .control-panel .btn-group .btn:first-child {
            border-top-left-radius: 0.25rem;
            border-bottom-left-radius: 0.25rem;
        }
        .control-panel .btn-group .btn:last-child {
            border-top-right-radius: 0.25rem;
            border-bottom-right-radius: 0.25rem;
        }
        .control-panel label {
            font-weight: 500;
            color: #6c757d;
        }
        .input-group-text {
            background-color: #f8f9fa;
        }
    </style>
</head>
<body>
    <c:set var="pageTitle" value="📊 Estadísticas" scope="request"/>
    <%@ include file="navbar.jsp" %>
    <div class="container container-extra-wide">

        <!-- Panel de Control de Tiempo Modificado -->
        <div class="card mb-4 p-3 control-panel shadow-sm">
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
                <div class="btn-group shadow-sm" role="group" aria-label="Rango de tiempo">
                    <button type="button" class="btn btn-outline-primary" data-range="1h" onclick="seleccionarRango('1h')">1h</button>
                    <button type="button" class="btn btn-outline-primary" data-range="24h" onclick="seleccionarRango('24h')">24h</button>
                    <button type="button" class="btn btn-outline-primary" data-range="7d" onclick="seleccionarRango('7d')">7 D</button>
                    <button type="button" class="btn btn-outline-primary" data-range="30d" onclick="seleccionarRango('30d')">30 D</button>
                    <button type="button" class="btn btn-outline-primary" data-range="all" onclick="seleccionarRango('all')">Todo</button>
                </div>

                <div class="d-flex align-items-center gap-2 bg-light p-2 rounded border">
                    <div class="input-group input-group-sm">
                        <span class="input-group-text"><i class="fas fa-calendar-alt"></i> &nbsp;Desde</span>
                        <input type="datetime-local" id="fechaInicio" class="form-control">
                    </div>
                    <div class="input-group input-group-sm ms-2">
                        <span class="input-group-text">Hasta</span>
                        <input type="datetime-local" id="fechaFin" class="form-control">
                    </div>
                    <button class="btn btn-primary btn-sm ms-2 px-3" onclick="aplicarFechasManual()">
                        <i class="fas fa-check"></i> Aplicar
                    </button>
                </div>
            </div>
        </div>

        <!-- Tarjetas de estadísticas (KPIs actuales) -->
        <div class="stats-grid mb-4">
            <div class="stat-card">
                <h3>Total Tarjetas</h3>
                <p class="value" id="kpi-total">-</p>
            </div>
            <div class="stat-card success">
                <h3>Estudiadas</h3>
                <p class="value" id="kpi-estudiadas">-</p>
            </div>
            <div class="stat-card info">
                <h3>Nuevas</h3>
                <p class="value" id="kpi-nuevas">-</p>
            </div>
            <div class="stat-card warning">
                <h3>Disponibles Ahora</h3>
                <p class="value" id="kpi-disponibles">-</p>
            </div>
            <div class="stat-card">
                <h3>En Reaprendizaje</h3>
                <p class="value" id="kpi-reaprendizaje">-</p>
            </div>
            <div class="stat-card success">
                <h3>Tasa de Aciertos</h3>
                <p class="value" id="kpi-tasa">-</p>
                <p class="tasa-aciertos" id="kpi-detalle">- / -</p>
            </div>
        </div>


        <div class="row g-4">
            <!-- Gráfico de aciertos vs fallos Histórico (Totales) -->
            <div class="col-md-12 col-lg-6">
                <div class="card p-3 h-100 shadow-sm">
                    <h5 class="card-title">Total Aciertos vs Fallos</h5>
                    <div class="chart-container" style="position: relative; height:300px;">
                        <canvas id="chartHistorico"></canvas>
                    </div>
                </div>
            </div>

            <!-- Gráfico de Porcentaje de Aciertos (Nuevo) -->
            <div class="col-md-12 col-lg-6">
                <div class="card p-3 h-100 shadow-sm">
                    <h5 class="card-title">Tasa de Acierto (%)</h5>
                    <div class="chart-container" style="position: relative; height:300px;">
                        <canvas id="chartPorcentaje"></canvas>
                    </div>
                </div>
            </div>

            <!-- Distribución Actual -->
            <div class="col-md-12 col-lg-4">
                <div class="card p-3 h-100 shadow-sm">
                    <h5 class="card-title">Distribución Actual</h5>
                    <div class="chart-container" style="position: relative; height:350px;">
                        <canvas id="distribucionChart"></canvas>
                    </div>
                </div>
            </div>

            <!-- Tiempo Promedio -->
            <div class="col-md-12 col-lg-8">
                <div class="card p-3 h-100 shadow-sm">
                    <h5 class="card-title">Tiempo Promedio de Respuesta (segundos)</h5>
                    <div class="chart-container" style="position: relative; height:300px;">
                        <canvas id="chartTiempos"></canvas>
                    </div>
                </div>
            </div>

            <!-- Porcentaje por Tipo -->
            <div class="col-md-12 col-lg-6">
                <div class="card p-3 h-100 shadow-sm">
                    <h5 class="card-title">Aciertos por Tipo de Palabra</h5>
                    <div class="chart-container" style="position: relative; height:350px;">
                        <canvas id="chartTipos"></canvas>
                    </div>
                </div>
            </div>

            <!-- Pronóstico de Revisiones -->
            <div class="col-md-12">
                <div class="card p-3 h-100 shadow-sm">
                    <h5 class="card-title">Carga de Revisiones (Pronóstico/Atrasadas)</h5>
                    <div class="chart-container" style="position: relative; height:300px;">
                        <canvas id="chartPronostico"></canvas>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // --- 1. CONFIGURACIÓN INICIAL Y ESTADO GLOBAL ---
        let charts = {}; // Almacena instancias de chart.js
        let rangoActual = localStorage.getItem('stats_rango') || '7d';

        document.addEventListener('DOMContentLoaded', () => {
            inicializarBotonesRango();
            seleccionarRango(rangoActual);

            // Cargar resumen de forma asíncrona
            cargarResumen();
        });

        // --- 2. LÓGICA DE SELECCIÓN DE TIEMPO ---

        function inicializarBotonesRango() {
            document.querySelectorAll('[data-range]').forEach(btn => {
                if (btn.dataset.range === rangoActual) btn.classList.add('active');
            });
        }

        function seleccionarRango(rango) {
            rangoActual = rango;
            localStorage.setItem('stats_rango', rango);

            // Actualizar UI botones
            document.querySelectorAll('[data-range]').forEach(btn => {
                btn.classList.toggle('active', btn.dataset.range === rango);
            });

            // Calcular fechas start/end
            const now = new Date();
            let start = new Date();
            let granularidad = 'dias';

            switch(rango) {
                case '1h':
                    start.setTime(now.getTime() - (1 * 60 * 60 * 1000));
                    granularidad = 'minutos';
                    break;
                case '24h':
                    start.setTime(now.getTime() - (24 * 60 * 60 * 1000));
                    granularidad = 'horas';
                    break;
                case '7d':
                    start.setDate(now.getDate() - 7);
                    granularidad = 'dias';
                    break;
                case '30d':
                    start.setDate(now.getDate() - 30);
                    granularidad = 'dias';
                    break;
                case 'all':
                    start = new Date('2023-01-01'); // Fecha muy antigua
                    granularidad = 'meses';
                    break;
            }

            // Actualizar inputs manuales
            document.getElementById('fechaInicio').value = toLocalISOString(start).slice(0, 16);
            document.getElementById('fechaFin').value = toLocalISOString(now).slice(0, 16);

            cargarTodosLosGraficos(start.getTime(), now.getTime(), granularidad);
        }

        function aplicarFechasManual() {
            const startStr = document.getElementById('fechaInicio').value;
            const endStr = document.getElementById('fechaFin').value;

            if (!startStr || !endStr) return;

            const start = new Date(startStr);
            const end = new Date(endStr);

            // Calcular granularidad dinámica
            const diffHours = (end - start) / (1000 * 60 * 60);
            let granularidad = 'dias';
            if (diffHours <= 2) granularidad = 'minutos';
            else if (diffHours <= 72) granularidad = 'horas';

            cargarTodosLosGraficos(start.getTime(), end.getTime(), granularidad);

            // Desmarcar botones predefinidos
            document.querySelectorAll('[data-range]').forEach(btn => btn.classList.remove('active'));
        }

        // Helper para timezone local en imputs datetime-local
        function toLocalISOString(date) {
            const pad = (n) => n < 10 ? '0' + n : n;
            return date.getFullYear() +
                '-' + pad(date.getMonth() + 1) +
                '-' + pad(date.getDate()) +
                'T' + pad(date.getHours()) +
                ':' + pad(date.getMinutes()) +
                ':' + pad(date.getSeconds());
        }

        // --- 3. CARGA DE DATOS (AJAX) ---

        function cargarTodosLosGraficos(startEpoch, endEpoch, granularidad) {
            const zonaHoraria = Intl.DateTimeFormat().resolvedOptions().timeZone;
            const params = `?inicio=\${startEpoch}&fin=\${endEpoch}&granularidad=\${granularidad}&zonaHoraria=\${encodeURIComponent(zonaHoraria)}`;

            // Nuevos endpoints separados
            fetchData('/api/stats/historico-totales' + params, 'chartHistorico', 'bar', { stacked: true });
            fetchData('/api/stats/historico-porcentaje' + params, 'chartPorcentaje', 'line', { fill: true, tension: 0.4, scales: { y: { min: 0, max: 100 } } });

            fetchData('/api/stats/tiempos' + params, 'chartTiempos', 'line', { tension: 0.4 });

            // Corrección gráfica tipos: Horizontal bar + Escala 0-100
            fetchData('/api/stats/tipos' + params, 'chartTipos', 'bar', {
                indexAxis: 'y',
                scales: {
                    x: { beginAtZero: true, max: 100 }
                }
             });

            fetchData('/api/stats/pronostico' + params, 'chartPronostico', 'bar');
        }

        async function fetchData(url, chartId, type, extraOptions = {}) {
            // Mostrar loading state si se quisiera (opcional)
            try {
                const response = await fetch(url);
                const data = await response.json();
                renderChart(chartId, type, data, extraOptions);
            } catch (error) {
                console.error("Error cargando chart " + chartId, error);
            }
        }

        // --- 4. RENDERIZADO CHART.JS ---

        function renderChart(canvasId, type, dto, extraOptions) {
            const ctx = document.getElementById(canvasId).getContext('2d');

            // Destruir anterior si existe
            if (charts[canvasId]) {
                charts[canvasId].destroy();
            }

            // Config base
            const config = {
                type: type,
                data: {
                    labels: dto.labels,
                    datasets: dto.datasets.map(ds => ({
                        label: ds.label,
                        data: ds.data,
                        backgroundColor: ds.backgroundColor,
                        borderColor: ds.borderColor,
                        borderWidth: 1,
                        fill: ds.fill
                    }))
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                         x: { stacked: extraOptions.stacked || false },
                         y: { stacked: extraOptions.stacked || false, beginAtZero: true }
                    },
                    plugins: {
                        legend: { position: 'top' },
                        tooltip: { mode: 'index', intersect: false }
                    },
                    ...extraOptions // Sobrescribir opciones específicas
                }
            };

            charts[canvasId] = new Chart(ctx, config);
        }

        // --- 5. RESUMEN Y DISTRIBUCIÓN (AJAX) ---
        async function cargarResumen() {
            try {
                const response = await fetch('/api/stats/resumen');
                const stats = await response.json();

                // Actualizar KPIs estáticos
                document.getElementById('kpi-total').textContent = stats.totalTarjetas;
                document.getElementById('kpi-estudiadas').textContent = stats.tarjetasEstudiadas;
                document.getElementById('kpi-nuevas').textContent = stats.tarjetasNuevas;
                document.getElementById('kpi-disponibles').textContent = stats.tarjetasDisponiblesAhora;
                document.getElementById('kpi-reaprendizaje').textContent = stats.tarjetasEnReaprendizaje;

                const tasa = stats.tasaAciertos ? stats.tasaAciertos : 0;
                document.getElementById('kpi-tasa').textContent = tasa.toFixed(1) + '%';
                document.getElementById('kpi-detalle').textContent = stats.totalAciertos + ' / ' + stats.totalRevisiones;

                renderDistribucionChart(stats);
            } catch (error) {
                console.error("Error al cargar resumen", error);
            }
        }

        function renderDistribucionChart(stats) {
            new Chart(document.getElementById('distribucionChart'), {
                type: 'doughnut',
                data: {
                    labels: ['Estudiadas', 'Nuevas', 'En Reaprendizaje'],
                    datasets: [{
                        data: [
                            stats.tarjetasEstudiadas - stats.tarjetasEnReaprendizaje,
                            stats.tarjetasNuevas,
                            stats.tarjetasEnReaprendizaje
                        ],
                        backgroundColor: ['#28a745', '#17a2b8', '#ffc107'],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } }
                }
            });
        }
    </script>
</body>
</html>

