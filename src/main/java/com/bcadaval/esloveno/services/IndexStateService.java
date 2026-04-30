package com.bcadaval.esloveno.services;

import com.bcadaval.esloveno.beans.Variable;
import com.bcadaval.esloveno.repo.VariablesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Gestiona el estado persistente del proceso de indexacion XML.
 */
@Service
public class IndexStateService {

    @Autowired
    private MensajeFlotanteService mensajeFlotanteService;

    public enum IndexStatus {
        NOT_STARTED,
        BUILDING,
        READY,
        ERROR
    }

    public static final String INDEX_STATUS = "INDEX_STATUS";
    public static final String INDEX_LAST_FILE_NUM = "INDEX_LAST_FILE_NUM";
    public static final String INDEX_LAST_ERROR = "INDEX_LAST_ERROR";

    @Autowired
    private VariablesRepo variablesRepo;

    public synchronized IndexStatus getStatus() {
        String value = getString(INDEX_STATUS, IndexStatus.NOT_STARTED.name());
        try {
            return IndexStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return IndexStatus.NOT_STARTED;
        }
    }

    public synchronized void setStatus(IndexStatus status) {
        saveString(INDEX_STATUS, status.name(), "STRING", "Estado del indexado XML");
        if(status == IndexStatus.READY) {
            mensajeFlotanteService.insertarMensaje("Ha acabado el indexado de los datos. Las búsquedas de nuevas palabras ahora serán más rápidas.");
        }
    }

    public synchronized int getLastFileNum() {
        String value = getString(INDEX_LAST_FILE_NUM, "0");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public synchronized void setLastFileNum(int fileNum) {
        saveString(INDEX_LAST_FILE_NUM, Integer.toString(fileNum), "INTEGER", "Ultimo archivo XML indexado por completo");
    }

    public synchronized void markBuilding() {
        setStatus(IndexStatus.BUILDING);
        saveString(INDEX_LAST_ERROR, "", "STRING", "Ultimo error de indexado XML");
    }

    public synchronized void markReady() {
        setStatus(IndexStatus.READY);
        saveString(INDEX_LAST_ERROR, "", "STRING", "Ultimo error de indexado XML");
    }

    public synchronized void markError(String errorMessage) {
        setStatus(IndexStatus.ERROR);
        saveString(INDEX_LAST_ERROR, errorMessage != null ? errorMessage : "", "STRING", "Ultimo error de indexado XML");
    }

    private String getString(String key, String defaultValue) {
        return variablesRepo.findByClave(key)
            .map(Variable::getValor)
            .orElse(defaultValue);
    }

    private void saveString(String key, String value, String tipo, String descripcion) {
        Variable var = variablesRepo.findByClave(key).orElseGet(Variable::new);
        var.setClave(key);
        var.setValor(value);
        var.setTipo(tipo);
        var.setDescripcion(descripcion);
        variablesRepo.save(var);
    }
}

