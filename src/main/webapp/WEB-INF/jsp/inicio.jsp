<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SloveneMaster - Inicio</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            color: #fff;
        }

        .container {
            text-align: center;
            padding: 40px;
            max-width: 600px;
            width: 90%;
        }

        .logo {
            font-size: 4rem;
            margin-bottom: 10px;
        }

        h1 {
            font-size: 2.5rem;
            margin-bottom: 10px;
            background: linear-gradient(90deg, #00d4ff, #7c3aed);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .subtitle {
            color: #a0a0a0;
            margin-bottom: 40px;
            font-size: 1.1rem;
        }

        /* Estado: Cargando */
        .loading-container {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 20px;
            padding: 40px;
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .progress-bar-container {
            width: 100%;
            height: 20px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 10px;
            overflow: hidden;
            margin-bottom: 20px;
        }

        .progress-bar {
            height: 100%;
            background: linear-gradient(90deg, #00d4ff, #7c3aed);
            border-radius: 10px;
            transition: width 0.3s ease;
            width: 0%;
        }

        .progress-text {
            font-size: 1.5rem;
            font-weight: bold;
            margin-bottom: 10px;
        }

        .status-message {
            color: #a0a0a0;
            font-size: 1rem;
            min-height: 24px;
        }

        /* Estado: Error */
        .error-container {
            background: rgba(239, 68, 68, 0.1);
            border: 1px solid rgba(239, 68, 68, 0.3);
            border-radius: 20px;
            padding: 40px;
        }

        .error-icon {
            font-size: 3rem;
            margin-bottom: 15px;
        }

        .error-message {
            color: #fca5a5;
            margin-bottom: 20px;
        }

        .retry-btn {
            background: linear-gradient(90deg, #ef4444, #dc2626);
            border: none;
            padding: 15px 40px;
            font-size: 1.1rem;
            color: white;
            border-radius: 30px;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        .retry-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 30px rgba(239, 68, 68, 0.3);
        }

        /* Estado: Listo */
        .ready-container {
            background: rgba(34, 197, 94, 0.1);
            border: 1px solid rgba(34, 197, 94, 0.3);
            border-radius: 20px;
            padding: 40px;
        }

        .ready-icon {
            font-size: 4rem;
            margin-bottom: 15px;
        }

        .start-btn {
            background: linear-gradient(90deg, #22c55e, #16a34a);
            border: none;
            padding: 20px 60px;
            font-size: 1.3rem;
            color: white;
            border-radius: 30px;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
            text-decoration: none;
            display: inline-block;
        }

        .start-btn:hover {
            transform: translateY(-3px);
            box-shadow: 0 15px 40px rgba(34, 197, 94, 0.4);
        }

        /* Animación de pulso para el botón */
        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.02); }
        }

        .start-btn {
            animation: pulse 2s infinite;
        }

        .start-btn:hover {
            animation: none;
        }

        /* Ocultar elementos */
        .hidden {
            display: none !important;
        }

        /* Spinner */
        .spinner {
            width: 40px;
            height: 40px;
            border: 4px solid rgba(255, 255, 255, 0.1);
            border-top-color: #00d4ff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 20px;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">🇸🇮</div>
        <h1>SloveneMaster</h1>
        <p class="subtitle">Aprende esloveno con repetición espaciada</p>

        <!-- Estado: Cargando -->
        <div id="loadingContainer" class="loading-container">
            <div class="spinner"></div>
            <div class="progress-bar-container">
                <div id="progressBar" class="progress-bar"></div>
            </div>
            <div id="progressText" class="progress-text">0%</div>
            <div id="statusMessage" class="status-message">Iniciando...</div>
        </div>

        <!-- Estado: Error -->
        <div id="errorContainer" class="error-container hidden">
            <div class="error-icon">❌</div>
            <h2>Error de inicialización</h2>
            <p id="errorMessage" class="error-message"></p>
            <button class="retry-btn" onclick="retryInit()">Reintentar</button>
        </div>

        <!-- Estado: Listo -->
        <div id="readyContainer" class="ready-container hidden">
            <div class="ready-icon">✅</div>
            <h2 style="margin-bottom: 20px;">¡Todo listo!</h2>
            <a href="/getWords" class="start-btn">🎓 Empezar a estudiar</a>
        </div>
    </div>

    <script>
        const loadingContainer = document.getElementById('loadingContainer');
        const errorContainer = document.getElementById('errorContainer');
        const readyContainer = document.getElementById('readyContainer');
        const progressBar = document.getElementById('progressBar');
        const progressText = document.getElementById('progressText');
        const statusMessage = document.getElementById('statusMessage');
        const errorMessage = document.getElementById('errorMessage');

        // Comprobar si ya está listo desde el servidor
        const initialReady = ${ready};

        if (initialReady) {
            showReady();
        } else {
            startPolling();
        }

        function showLoading() {
            loadingContainer.classList.remove('hidden');
            errorContainer.classList.add('hidden');
            readyContainer.classList.add('hidden');
        }

        function showError(message) {
            loadingContainer.classList.add('hidden');
            errorContainer.classList.remove('hidden');
            readyContainer.classList.add('hidden');
            errorMessage.textContent = message || 'Error desconocido';
        }

        function showReady() {
            loadingContainer.classList.add('hidden');
            errorContainer.classList.add('hidden');
            readyContainer.classList.remove('hidden');
        }

        function updateProgress(percent, message) {
            progressBar.style.width = percent + '%';
            progressText.textContent = percent + '%';
            statusMessage.textContent = message || '';
        }

        let pollingInterval = null;

        function startPolling() {
            showLoading();
            pollingInterval = setInterval(checkStatus, 500);
            checkStatus(); // Ejecutar inmediatamente
        }

        function stopPolling() {
            if (pollingInterval) {
                clearInterval(pollingInterval);
                pollingInterval = null;
            }
        }

        async function checkStatus() {
            try {
                const response = await fetch('/api/init/status');
                const data = await response.json();

                updateProgress(data.progress, data.message);

                if (data.ready || data.status === 'COMPLETED') {
                    stopPolling();
                    showReady();
                } else if (data.status === 'ERROR') {
                    stopPolling();
                    showError(data.error);
                }
            } catch (error) {
                console.error('Error checking status:', error);
            }
        }

        async function retryInit() {
            showLoading();
            updateProgress(0, 'Reintentando...');

            try {
                await fetch('/api/init/start', { method: 'POST' });
                startPolling();
            } catch (error) {
                showError('Error al reiniciar: ' + error.message);
            }
        }
    </script>
</body>
</html>

