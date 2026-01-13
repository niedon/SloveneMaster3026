package com.bcadaval.esloveno.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcadaval.esloveno.beans.Variable;

/**
 * Repositorio para acceder a las variables de configuración del sistema
 */
@Repository
public interface VariablesRepo extends JpaRepository<Variable, String> {

    /**
     * Busca una variable por su clave
     * @param clave Clave de la variable
     * @return Optional con la variable si existe
     */
    Optional<Variable> findByClave(String clave);
}

