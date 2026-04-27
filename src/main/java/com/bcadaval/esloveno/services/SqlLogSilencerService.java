package com.bcadaval.esloveno.services;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Silencia temporalmente logs SQL verbosos durante tareas puntuales.
 */
@Service
public class SqlLogSilencerService {

    private static final String[] SQL_LOGGERS = {
        "org.hibernate.SQL",
        "org.springframework.jdbc.core.JdbcTemplate",
        "org.springframework.jdbc.core.StatementCreatorUtils"
    };

    public AutoCloseable silenceForIndexing() {
        Map<String, Level> previous = new HashMap<>();
        for (String loggerName : SQL_LOGGERS) {
            org.slf4j.Logger slf4j = LoggerFactory.getLogger(loggerName);
            if (slf4j instanceof Logger logger) {
                previous.put(loggerName, logger.getLevel());
                logger.setLevel(Level.WARN);
            }
        }

        return () -> {
            for (Map.Entry<String, Level> entry : previous.entrySet()) {
                org.slf4j.Logger slf4j = LoggerFactory.getLogger(entry.getKey());
                if (slf4j instanceof Logger logger) {
                    logger.setLevel(entry.getValue());
                }
            }
        };
    }
}

