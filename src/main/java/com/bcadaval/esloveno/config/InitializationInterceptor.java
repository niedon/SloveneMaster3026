package com.bcadaval.esloveno.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bcadaval.esloveno.services.InitializationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.log4j.Log4j2;

/**
 * Interceptor que verifica si el sistema está inicializado antes de acceder a cualquier página.
 * Si no está inicializado, redirige a la página de inicio para completar la inicialización.
 */
@Log4j2
@Configuration
public class InitializationInterceptor implements WebMvcConfigurer, HandlerInterceptor {

    @Autowired
    private InitializationService initializationService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",                    // Página de inicio
                        "/api/init/**",         // APIs de inicialización
                        "/WEB-INF/**",          // Recursos internos
                        "/css/**",              // CSS
                        "/js/**",               // JavaScript
                        "/images/**",           // Imágenes
                        "/error"                // Página de error
                );
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Comprobación rápida: solo verifica existencia de archivos
        if (!initializationService.isFullyReady()) {
            log.debug("Sistema no inicializado, redirigiendo a / desde {}", request.getRequestURI());
            response.sendRedirect("/");
            return false;
        }
        return true;
    }
}

