package com.bcadaval.esloveno.repo;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class IndiceXmlDao {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public IndiceXmlDao(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Búsqueda hiper-rápida usando índice
     */
    public List<IndiceXmlDTO> buscarPorLema(String lema) {
        String sql = "SELECT LEMA, TIPO, SLOLEKS_ID, ARCHIVO_NUM FROM INDICE_XML WHERE LEMA = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new IndiceXmlDTO(
                rs.getString("LEMA"),
                rs.getString("TIPO"),
                rs.getString("SLOLEKS_ID"),
                rs.getInt("ARCHIVO_NUM")
        ), lema);
    }

    /**
     * Verifica si el índice está vacío
     */
    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INDICE_XML", Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Borra todos los datos del índice
     */
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM INDICE_XML");
    }

    /**
     * Inserción masiva de índices
     */
    public void batchInsert(List<IndiceXmlDTO> loteArchivo) {
        String sql = "INSERT INTO INDICE_XML (LEMA, TIPO, SLOLEKS_ID, ARCHIVO_NUM) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                IndiceXmlDTO dto = loteArchivo.get(i);
                ps.setString(1, dto.lema());
                ps.setString(2, dto.tipo());
                ps.setString(3, dto.sloleksId());
                ps.setInt(4, dto.archivoNum());
            }

            @Override
            public int getBatchSize() {
                return loteArchivo.size();
            }
        });
    }

    /**
     * Borra todas las entradas indexadas para un archivo XML específico.
     */
    public void deleteByArchivoNum(int archivoNum) {
        jdbcTemplate.update("DELETE FROM INDICE_XML WHERE ARCHIVO_NUM = ?", archivoNum);
    }

    /**
     * Reemplaza de forma atómica el índice de un archivo concreto.
     */
    public void replaceArchivoIndex(int archivoNum, List<IndiceXmlDTO> loteArchivo) {
        transactionTemplate.executeWithoutResult(status -> {
            deleteByArchivoNum(archivoNum);
            if (!loteArchivo.isEmpty()) {
                batchInsert(loteArchivo);
            }
        });
    }
}
