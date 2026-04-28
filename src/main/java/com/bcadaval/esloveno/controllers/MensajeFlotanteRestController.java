package com.bcadaval.esloveno.controllers;

import com.bcadaval.esloveno.beans.MensajeFlotante;
import com.bcadaval.esloveno.services.MensajeFlotanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeFlotanteRestController {

    @Autowired
    private MensajeFlotanteService service;

    @GetMapping("/noleidos")
    public ResponseEntity<List<MensajeFlotante>> getNoLeidos() {
        return ResponseEntity.ok(service.obtenerNoLeidos());
    }

    @PostMapping("/{id}/marcar-leido")
    public ResponseEntity<Void> marcarLeido(@PathVariable Long id) {
        service.marcarComoLeido(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Integer>> getConfig() {
        return ResponseEntity.ok(Map.of("intervaloSegundos", MensajeFlotanteService.INTERVALO_REFRESCO_SEGUNDOS));
    }
}

