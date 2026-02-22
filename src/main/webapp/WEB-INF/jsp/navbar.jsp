<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Barra de navegación flotante.
  Incluir con: <%@ include file="navbar.jsp" %>
  Parámetro esperado: pageTitle (opcional)
--%>
<nav class="navbar" id="floatingNavbar">
    <!-- Botón hamburguesa flotante -->
    <button class="navbar-toggle" onclick="toggleNavbar()" aria-label="Menú" aria-expanded="false" aria-controls="navbarPanel">
        <span class="navbar-toggle-icon"></span>
    </button>

    <!-- Panel expandido -->
    <div class="navbar-container" id="navbarPanel">
        <a href="/getWords" class="navbar-brand">
            <c:choose>
                <c:when test="${not empty pageTitle}">Esloveno · ${pageTitle}</c:when>
                <c:otherwise>Esloveno</c:otherwise>
            </c:choose>
        </a>
        <ul class="navbar-menu">
            <li><a href="/getWords"        class="navbar-link">📖 Estudio</a></li>
            <li><a href="/anadirPalabras"  class="navbar-link">📚 Añadir</a></li>
            <li><a href="/completarPalabras" class="navbar-link">📝 Completar</a></li>
            <li><a href="/configuracion"   class="navbar-link">⚙️ Configuración</a></li>
            <li><a href="/estadisticas"    class="navbar-link">📊 Estadísticas</a></li>
        </ul>
    </div>
</nav>
<script>
function toggleNavbar() {
    const nav = document.getElementById('floatingNavbar');
    const btn = nav.querySelector('.navbar-toggle');
    const isOpen = nav.classList.toggle('open');
    btn.setAttribute('aria-expanded', isOpen);
}

// Cerrar al pulsar fuera del navbar
document.addEventListener('click', function(e) {
    const nav = document.getElementById('floatingNavbar');
    if (nav && nav.classList.contains('open') && !nav.contains(e.target)) {
        nav.classList.remove('open');
        nav.querySelector('.navbar-toggle').setAttribute('aria-expanded', 'false');
    }
});
</script>
