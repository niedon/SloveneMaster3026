<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Estudio - Esloveno</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>
    <c:set var="pageTitle" value="📖 Estudio de Palabras" scope="request"/>
    <%@ include file="navbar.jsp" %>
    <div class="container">

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

        <c:set var="tamanio" value="${fn:length(datos)}" />

        <form action="/enviarRespuestas" method="POST" id="respuestasForm">
            <table>
                <thead>
                    <tr>
                        <th colspan="${tamanio}">Palabras</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Fila 1: pregunta (transparente al idioma) -->
                    <tr class="row-visible">
                        <c:forEach var="dato" items="${datos}">
                            <td class="text-center">
                                <strong><c:out value="${dato.textoFila1}" /></strong>
                            </td>
                        </c:forEach>
                    </tr>

                    <!-- Fila 2: respuesta y botones (colapsada) -->
                    <tr class="row-collapsed collapsed" id="row-details">
                        <c:forEach var="dato" items="${datos}" varStatus="status">
                            <td class="text-center">
                                <strong><c:out value="${dato.textoFila2}" /></strong>
                                <br/>
                                <div class="button-container">
                                    <button type="button" class="btn-abajo btn-danger" id="btn_abajo_${status.index}"
                                            onclick="setResponse(${status.index})"
                                            <c:if test="${dato.id == null}">style='visibility:hidden' disabled</c:if>>👎</button>
                                    <button type="button" class="btn-arriba btn-success" id="btn_arriba_${status.index}"
                                            onclick="setResponse(${status.index})"
                                            <c:if test="${dato.id == null}">style='visibility:hidden' disabled</c:if>>👍</button>

                                </div>
                                <c:if test="${dato.id != null}">
                                    <input type="hidden" name="tipo_${status.index}" value="${dato.tipo.codigo}">
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
    </div>

    <script>
        function showDetails() {
            const detailsRow = document.getElementById('row-details');
            detailsRow.classList.remove('collapsed');
            detailsRow.classList.add('visible');
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

