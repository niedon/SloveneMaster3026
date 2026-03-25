<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Estudio - Esloveno</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css?v=${cssVersion}">
    <%@ include file="head-favicon.jsp" %>
</head>
<body>
    <c:set var="pageTitle" value="📖 Estudio de Palabras" scope="request"/>
    <%@ include file="navbar.jsp" %>
    <div class="container">

        <c:set var="tamanio" value="${fn:length(datos)}" />

        <form action="/enviarRespuestas" method="POST" id="respuestasForm">
            <table class="estudio-table">
                <tbody>
                    <!-- Fila 1: pregunta (transparente al idioma) -->
                    <tr class="row-visible">
                        <c:forEach var="dato" items="${datos}">
                            <td class="text-center<c:if test='${dato.id != null}'> celda-srs</c:if>">
                                <strong><c:out value="${dato.textoFila1}" /></strong>
                            </td>
                        </c:forEach>
                    </tr>

                    <!-- Fila 2: respuesta y botones (colapsada) -->
                    <tr class="row-collapsed" id="row-details">
                        <c:forEach var="dato" items="${datos}" varStatus="status">
                            <td class="text-center<c:if test='${dato.id != null}'> celda-srs</c:if>">
                                <strong><c:out value="${dato.textoFila2}" /></strong>

                                <!-- Botones y próx estudio -->
                                <c:if test="${dato.id != null}">
                                    <br/>
                                    <div class="button-container">
                                        <button type="button" class="btn-abajo btn-danger<c:if test='${dato.id == null}'> btn-invisible</c:if>" id="btn_abajo_${status.index}"
                                                onclick="setResponse(${status.index})"
                                                <c:if test="${dato.id == null}">disabled</c:if>>👎</button>
                                        <button type="button" class="btn-arriba btn-success<c:if test='${dato.id == null}'> btn-invisible</c:if>" id="btn_arriba_${status.index}"
                                                onclick="setResponse(${status.index})"
                                                <c:if test="${dato.id == null}">disabled</c:if>>👍</button>
                                    </div>

                                    <div id="intervalo_${status.index}" class="intervalo-texto">
                                        <span id="intervalo_texto_${status.index}"></span>
                                    </div>
                                    <input type="hidden" id="intervalo_arriba_${status.index}" value="${dato.intervaloArriba}">
                                    <input type="hidden" id="intervalo_abajo_${status.index}" value="${dato.intervaloAbajo}">
                                    <input type="hidden" name="tipo_${status.index}" value="${dato.tipo.xmlCode}">
                                    <input type="hidden" name="id_${status.index}" value="${dato.id}">
                                    <input type="hidden" id="valor_${status.index}" name="valor_${status.index}" value="" class="valor-input">
                                </c:if>
                            </td>
                        </c:forEach>
                    </tr>
                </tbody>
            </table>

            <div class="text-center">
                <button type="button" class="btn-secondary" onclick="showDetails(); return false;">Mostrar respuestas</button>
                <button type="submit" class="btn-primary submit-btn">Enviar Respuestas</button>
            </div>
        </form>

        <!-- Contador de tarjetas disponibles -->
        <div class="tarjetas-info">
            <c:choose>
                <c:when test="${tarjetasDisponibles > 0}">
                    <p class="tarjetas-disponibles">📚 Hay <strong>${tarjetasDisponibles}</strong> palabra(s) disponible(s) para el estudio</p>
                </c:when>
                <c:when test="${tarjetasNuevas > 0}">
                    <p class="tarjetas-nuevas">🆕 Hay <strong>${tarjetasNuevas}</strong> palabra(s) nueva(s) para aprender</p>
                </c:when>
                <c:otherwise>
                    <p class="sin-tarjetas">✅ ¡No hay tarjetas pendientes! Vuelve más tarde.</p>
                </c:otherwise>
            </c:choose>
        </div>

    </div>

    <script>
        function showDetails() {
            const detailsRow = document.getElementById('row-details');
            detailsRow.classList.remove('row-collapsed');
        }

        function setResponse(index, respuesta) {
            if (!respuesta) {
                const btnArriba = document.getElementById('btn_arriba_' + index);
                const btnAbajo = document.getElementById('btn_abajo_' + index);

                if (event.target.id === 'btn_arriba_' + index) {
                    respuesta = 'arriba';
                } else if (event.target.id === 'btn_abajo_' + index) {
                    respuesta = 'abajo';
                }
            }

            document.getElementById('valor_' + index).value = respuesta;

            const btnArriba = document.getElementById('btn_arriba_' + index);
            const btnAbajo = document.getElementById('btn_abajo_' + index);

            if (respuesta === 'arriba') {
                btnArriba.classList.add('selected');
                btnAbajo.classList.remove('selected');
            } else if (respuesta === 'abajo') {
                btnAbajo.classList.add('selected');
                btnArriba.classList.remove('selected');
            }

            // Mostrar el intervalo correspondiente
            const intervaloContainer = document.getElementById('intervalo_' + index);
            const intervaloTexto = document.getElementById('intervalo_texto_' + index);
            const intervaloArriba = document.getElementById('intervalo_arriba_' + index);
            const intervaloAbajo = document.getElementById('intervalo_abajo_' + index);

            if (intervaloContainer && intervaloTexto && intervaloArriba && intervaloAbajo) {
                if (respuesta === 'arriba' && intervaloArriba.value) {
                    intervaloTexto.textContent = '⏰ Próx: ' + intervaloArriba.value;
                    intervaloContainer.classList.add('intervalo-visible');
                } else if (respuesta === 'abajo' && intervaloAbajo.value) {
                    intervaloTexto.textContent = '⏰ Próx: ' + intervaloAbajo.value;
                    intervaloContainer.classList.add('intervalo-visible');
                } else {
                    intervaloContainer.classList.remove('intervalo-visible');
                }
            }

            const detailsRow = document.getElementById('row-details');
            if (detailsRow.classList.contains('collapsed')) {
                showDetails();
            }
        }

        document.getElementById('respuestasForm').addEventListener('submit', function(event) {
            const valorInputs = document.querySelectorAll('.valor-input');
            let allCompleted = true;

            valorInputs.forEach(input => {
                if (input.value === '') {
                    allCompleted = false;
                }
            });

            if (!allCompleted) {
                event.preventDefault();
                alert('Quedan palabras por puntuar');
                return false;
            }
        });
    </script>
</body>
</html>

