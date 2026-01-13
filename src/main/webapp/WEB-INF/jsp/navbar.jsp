<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Barra de navegación común para todas las páginas.
  Incluir con: <%@ include file="navbar.jsp" %>
  Parámetro esperado: pageTitle (opcional)
--%>
<nav class="navbar">
    <div class="navbar-container">
        <a href="/getWords" class="navbar-brand">
            <c:choose>
                <c:when test="${not empty pageTitle}">Esloveno - ${pageTitle}</c:when>
                <c:otherwise>Esloveno</c:otherwise>
            </c:choose>
        </a>
        <button class="navbar-toggle" onclick="toggleNavbar()" aria-label="Menú">
            <span class="navbar-toggle-icon"></span>
        </button>
        <ul class="navbar-menu" id="navbarMenu">
            <li><a href="/getWords" class="navbar-link">📖 Estudio</a></li>
            <li><a href="/anadirPalabras" class="navbar-link">📚 Añadir</a></li>
            <li><a href="/completarPalabras" class="navbar-link">📝 Completar</a></li>
            <li><a href="/configuracion" class="navbar-link">⚙️ Configuración</a></li>
            <li><a href="/estadisticas" class="navbar-link">📊 Estadísticas</a></li>
        </ul>
    </div>
</nav>
<script>
function toggleNavbar() {
    const menu = document.getElementById('navbarMenu');
    menu.classList.toggle('active');
}
</script>

