<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Estadísticas de Estudio</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <%@ include file="head-favicon.jsp" %>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <c:set var="pageTitle" value="📊 Estadísticas" scope="request"/>
    <%@ include file="navbar.jsp" %>
    <div class="container container-extra-wide">


        <!-- Tarjetas de estadísticas -->
        <div class="stats-grid">
            <div class="stat-card">
                <h3>Total Tarjetas</h3>
                <p class="value">${estadisticas.totalTarjetas}</p>
            </div>
            <div class="stat-card success">
                <h3>Estudiadas</h3>
                <p class="value">${estadisticas.tarjetasEstudiadas}</p>
            </div>
            <div class="stat-card info">
                <h3>Nuevas</h3>
                <p class="value">${estadisticas.tarjetasNuevas}</p>
            </div>
            <div class="stat-card warning">
                <h3>Disponibles Ahora</h3>
                <p class="value">${estadisticas.tarjetasDisponiblesAhora}</p>
            </div>
            <div class="stat-card">
                <h3>En Reaprendizaje</h3>
                <p class="value">${estadisticas.tarjetasEnReaprendizaje}</p>
            </div>
            <div class="stat-card success">
                <h3>Tasa de Aciertos</h3>
                <p class="value">${String.format("%.1f", estadisticas.tasaAciertos)}%</p>
                <p class="tasa-aciertos">${estadisticas.totalAciertos} / ${estadisticas.totalRevisiones}</p>
            </div>
        </div>

        <!-- Gráficos -->
        <div class="charts-container">
            <!-- Gráfico de distribución de tarjetas -->
            <div class="chart-box">
                <h3>Distribución de Tarjetas</h3>
                <div class="chart-wrapper">
                    <canvas id="distribucionChart"></canvas>
                </div>
            </div>

            <!-- Gráfico de aciertos vs fallos -->
            <div class="chart-box">
                <h3>Aciertos vs Fallos</h3>
                <div class="chart-wrapper">
                    <canvas id="aciertosChart"></canvas>
                </div>
            </div>

            <!-- Gráfico de progreso -->
            <div class="chart-box">
                <h3>Estado del Estudio</h3>
                <div class="chart-wrapper">
                    <canvas id="progresoChart"></canvas>
                </div>
            </div>

            <!-- Gráfico de tarjetas por estado -->
            <div class="chart-box">
                <h3>Tarjetas por Estado</h3>
                <div class="chart-wrapper">
                    <canvas id="estadoChart"></canvas>
                </div>
            </div>
        </div>
    </div>

    <script>
        // Datos del servidor
        const stats = {
            totalTarjetas: ${estadisticas.totalTarjetas},
            tarjetasEstudiadas: ${estadisticas.tarjetasEstudiadas},
            tarjetasNuevas: ${estadisticas.tarjetasNuevas},
            tarjetasDisponiblesAhora: ${estadisticas.tarjetasDisponiblesAhora},
            tarjetasEnReaprendizaje: ${estadisticas.tarjetasEnReaprendizaje},
            totalRevisiones: ${estadisticas.totalRevisiones},
            totalAciertos: ${estadisticas.totalAciertos},
            tasaAciertos: ${estadisticas.tasaAciertos}
        };

        // Colores consistentes
        const colors = {
            primary: '#667eea',
            success: '#28a745',
            warning: '#ffc107',
            danger: '#dc3545',
            info: '#17a2b8',
            secondary: '#6c757d'
        };

        // Gráfico de distribución (Donut)
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
                    backgroundColor: [colors.success, colors.info, colors.warning],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });

        // Gráfico de aciertos vs fallos (Pie)
        const fallos = stats.totalRevisiones - stats.totalAciertos;
        new Chart(document.getElementById('aciertosChart'), {
            type: 'pie',
            data: {
                labels: ['Aciertos', 'Fallos'],
                datasets: [{
                    data: [stats.totalAciertos, fallos],
                    backgroundColor: [colors.success, colors.danger],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });

        // Gráfico de progreso (Barra horizontal)
        new Chart(document.getElementById('progresoChart'), {
            type: 'bar',
            data: {
                labels: ['Progreso'],
                datasets: [
                    {
                        label: 'Estudiadas',
                        data: [stats.tarjetasEstudiadas],
                        backgroundColor: colors.success
                    },
                    {
                        label: 'Pendientes',
                        data: [stats.tarjetasNuevas],
                        backgroundColor: colors.secondary
                    }
                ]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                },
                scales: {
                    x: {
                        stacked: true,
                        max: stats.totalTarjetas
                    },
                    y: {
                        stacked: true
                    }
                }
            }
        });

        // Gráfico de estado (Barras)
        new Chart(document.getElementById('estadoChart'), {
            type: 'bar',
            data: {
                labels: ['Disponibles', 'Nuevas', 'Reaprendizaje', 'Estudiadas'],
                datasets: [{
                    label: 'Tarjetas',
                    data: [
                        stats.tarjetasDisponiblesAhora,
                        stats.tarjetasNuevas,
                        stats.tarjetasEnReaprendizaje,
                        stats.tarjetasEstudiadas
                    ],
                    backgroundColor: [
                        colors.warning,
                        colors.info,
                        colors.danger,
                        colors.success
                    ],
                    borderWidth: 0,
                    borderRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    </script>
</body>
</html>

