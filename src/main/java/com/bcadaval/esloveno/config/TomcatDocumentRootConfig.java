package com.bcadaval.esloveno.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.File;

/**
 * Configuración de Tomcat para Docker
 */
@Configuration
public class TomcatDocumentRootConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> dockerTomcatCustomizer() {
        return factory -> {
            factory.setDocumentRoot(new File("/app"));
        };
    }
}

