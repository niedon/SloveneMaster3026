package com.bcadaval.esloveno.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
@Service
public class InitializationService {

    private static final String SLOLEKS_URL = "https://www.clarin.si/repository/xmlui/bitstream/handle/11356/1745/Sloleks.3.0.zip";
    private static final int MAX_RETRIES = 3;

    @Value("${app.db.path:/data/esloveno.db}")
    private String dbPath;

    @Value("${app.xml.path:/data/xml}")
    private String xmlPath;

    @Lazy
    @Autowired
    private DatosInicialesService datosInicialesService;

    public enum InitStatus {
        PENDING, IN_PROGRESS, COMPLETED, ERROR
    }

    @Getter
    private final AtomicReference<InitStatus> status = new AtomicReference<>(InitStatus.PENDING);

    @Getter
    private final AtomicInteger progress = new AtomicInteger(0);

    @Getter
    private final AtomicReference<String> message = new AtomicReference<>("Esperando inicialización...");

    @Getter
    private final AtomicReference<String> errorMessage = new AtomicReference<>(null);

    @PostConstruct
    public void init() {
        log.info("InitializationService configurado - DB: {}, XML: {}", dbPath, xmlPath);
    }

    /**
     * Comprobación rápida de si la BD existe (para interceptor)
     */
    public boolean isDatabaseReady() {
        return Files.exists(Path.of(dbPath)) && Files.isReadable(Path.of(dbPath));
    }

    /**
     * Comprobación rápida de si hay al menos un XML (para interceptor)
     */
    public boolean isXmlReady() {
        Path xmlDir = Path.of(xmlPath);
        if (!Files.exists(xmlDir) || !Files.isDirectory(xmlDir)) {
            return false;
        }
        try (Stream<Path> files = Files.list(xmlDir)) {
            return files.anyMatch(p -> p.getFileName().toString().startsWith("sloleks_")
                    && p.getFileName().toString().endsWith(".xml"));
        } catch (IOException e) {
            log.warn("Error comprobando XMLs: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Comprueba si todo está listo para usar la aplicación
     * (BD existe, XMLs existen, y BD tiene datos)
     */
    public boolean isFullyReady() {
        if (!isDatabaseReady() || !isXmlReady()) {
            return false;
        }

        // Verificar si hay datos en la BD
        try {
            return datosInicialesService.hayDatosEnBD();
        } catch (Exception e) {
            log.warn("Error verificando datos en BD: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Inicia el proceso de inicialización en un hilo separado
     */
    public synchronized void startInitialization() {
        if (status.get() == InitStatus.IN_PROGRESS) {
            log.info("Inicialización ya en progreso, ignorando solicitud");
            return;
        }

        status.set(InitStatus.IN_PROGRESS);
        progress.set(0);
        errorMessage.set(null);

        Thread initThread = new Thread(this::runInitialization, "InitializationThread");
        initThread.setDaemon(true);
        initThread.start();
    }

    private void runInitialization() {
        try {
            boolean needsXml = !isXmlReady();

            log.info("Iniciando inicialización - XML necesario: {}", needsXml);

            // La BD es creada automáticamente por Spring con spring.sql.init
            // Solo necesitamos asegurar que el directorio existe
            ensureDataDirectoryExists();
            progress.set(10);
            message.set("Base de datos lista");

            if (needsXml) {
                downloadAndExtractXml();
            } else {
                log.info("XMLs ya existen, omitiendo descarga");
                progress.set(100);
            }

            // Cargar datos iniciales si la BD está vacía
            message.set("Verificando datos iniciales...");
            progress.set(90);
            datosInicialesService.cargarDatosInicialesSiNecesario(
                progress::set,      // Callback para actualizar el progreso
                message::set        // Callback para actualizar el mensaje
            );

            status.set(InitStatus.COMPLETED);
            message.set("¡Inicialización completada!");
            progress.set(100);
            log.info("Inicialización completada exitosamente");

        } catch (Exception e) {
            log.error("Error durante la inicialización", e);
            status.set(InitStatus.ERROR);
            errorMessage.set(e.getMessage());
            message.set("Error: " + e.getMessage());
        }
    }

    private void ensureDataDirectoryExists() throws IOException {
        message.set("Preparando directorios...");
        progress.set(5);

        // Asegurar que el directorio de datos existe
        Path dbPathFile = Path.of(dbPath);
        Files.createDirectories(dbPathFile.getParent());

        Path xmlDir = Path.of(xmlPath);
        Files.createDirectories(xmlDir);

        log.info("Directorios de datos preparados");
    }

    private void downloadAndExtractXml() throws IOException {
        message.set("Preparando descarga de archivos XML...");
        progress.set(15);

        Path xmlDir = Path.of(xmlPath);
        Files.createDirectories(xmlDir);

        Path tempZip = Files.createTempFile("sloleks", ".zip");

        try {
            // Descargar con reintentos
            downloadWithRetry(tempZip);

            // Extraer archivos
            extractXmlFiles(tempZip, xmlDir);

        } finally {
            // Limpiar archivo temporal
            Files.deleteIfExists(tempZip);
        }
    }

    private void downloadWithRetry(Path destination) throws IOException {
        int attempt = 0;
        IOException lastException = null;

        while (attempt < MAX_RETRIES) {
            attempt++;
            try {
                message.set(String.format("Descargando archivos (intento %d/%d)...", attempt, MAX_RETRIES));
                log.info("Intento de descarga {}/{}", attempt, MAX_RETRIES);

                downloadFile(destination);
                return; // Éxito

            } catch (IOException e) {
                lastException = e;
                log.warn("Error en intento {}: {}", attempt, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    // Espera exponencial: 1s, 2s, 4s
                    long waitTime = (long) Math.pow(2, attempt - 1) * 1000;
                    message.set(String.format("Reintentando en %d segundos...", waitTime / 1000));
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Descarga interrumpida", ie);
                    }
                }
            }
        }

        throw new IOException("Fallo después de " + MAX_RETRIES + " intentos: " +
                (lastException != null ? lastException.getMessage() : "Error desconocido"));
    }

    private void downloadFile(Path destination) throws IOException {
        URL url = URI.create(SLOLEKS_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("User-Agent", "SloveneMaster/1.0");

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP Error: " + responseCode);
            }

            long totalSize = conn.getContentLengthLong();
            log.info("Tamaño del archivo: {} bytes", totalSize);

            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(destination))) {

                byte[] buffer = new byte[8192];
                long downloaded = 0;
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;

                    if (totalSize > 0) {
                        // Progreso de descarga: 15% a 70%
                        int downloadProgress = (int) (15 + (downloaded * 55 / totalSize));
                        progress.set(Math.min(downloadProgress, 70));
                        message.set(String.format("Descargando... %.1f%%", (downloaded * 100.0 / totalSize)));
                    }
                }
            }

            log.info("Descarga completada: {} bytes", Files.size(destination));

        } finally {
            conn.disconnect();
        }
    }

    private void extractXmlFiles(Path zipPath, Path targetDir) throws IOException {
        message.set("Descomprimiendo archivos...");
        progress.set(75);
        log.info("Extrayendo archivos XML de {}", zipPath);

        int extractedCount = 0;

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipPath)))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // Solo extraer archivos sloleks_*.xml de la carpeta Sloleks.3.0/
                // Ignorar: xml_schemas/, 00README.txt
                if (!entry.isDirectory() && name.contains("sloleks_") && name.endsWith(".xml")) {
                    // Obtener solo el nombre del archivo (sin la ruta del ZIP)
                    String fileName = Path.of(name).getFileName().toString();
                    Path outputPath = targetDir.resolve(fileName);

                    log.debug("Extrayendo: {} -> {}", name, outputPath);

                    try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(outputPath))) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }

                    extractedCount++;
                    // Progreso de extracción: 75% a 95%
                    progress.set(Math.min(75 + extractedCount, 95));
                    message.set(String.format("Descomprimiendo... (%d archivos)", extractedCount));
                }

                zis.closeEntry();
            }
        }

        log.info("Extracción completada: {} archivos XML", extractedCount);
        progress.set(100);
        message.set("Archivos listos");

        if (extractedCount == 0) {
            throw new IOException("No se encontraron archivos XML en el ZIP");
        }
    }

    /**
     * Obtiene el estado actual como DTO para la API
     */
    public InitStatusDTO getStatusDTO() {
        return new InitStatusDTO(
                status.get().name(),
                progress.get(),
                message.get(),
                errorMessage.get(),
                isFullyReady()
        );
    }

    public record InitStatusDTO(
            String status,
            int progress,
            String message,
            String error,
            boolean ready
    ) {}
}

