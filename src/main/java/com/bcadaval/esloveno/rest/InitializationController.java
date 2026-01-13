package com.bcadaval.esloveno.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcadaval.esloveno.services.InitializationService;
import com.bcadaval.esloveno.services.InitializationService.InitStatusDTO;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class InitializationController {

    @Autowired
    private InitializationService initializationService;

    /**
     * Página principal - muestra la pantalla de inicio/inicialización
     */
    @GetMapping("/")
    public String index(Model model) {
        boolean ready = initializationService.isFullyReady();
        model.addAttribute("ready", ready);

        if (!ready) {
            log.info("Sistema no inicializado, mostrando pantalla de carga");
            // Iniciar automáticamente si no está en progreso
            if (initializationService.getStatus().get() == InitializationService.InitStatus.PENDING) {
                initializationService.startInitialization();
            }
        }

        return "inicio";
    }

    /**
     * API para obtener el estado de inicialización (polling)
     */
    @GetMapping("/api/init/status")
    @ResponseBody
    public ResponseEntity<InitStatusDTO> getStatus() {
        return ResponseEntity.ok(initializationService.getStatusDTO());
    }

    /**
     * API para iniciar/reiniciar la inicialización manualmente
     */
    @PostMapping("/api/init/start")
    @ResponseBody
    public ResponseEntity<InitStatusDTO> startInit() {
        log.info("Solicitud de inicio de inicialización");
        initializationService.startInitialization();
        return ResponseEntity.ok(initializationService.getStatusDTO());
    }
}

