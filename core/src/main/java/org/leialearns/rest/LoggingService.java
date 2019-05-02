package org.leialearns.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.leialearns.model.LoggingLevel;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@Component
@Slf4j
public class LoggingService implements LoggingApiDelegate {

    private final ObjectMapper yamlObjectMapper;

    public LoggingService(@Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
        this.yamlObjectMapper = yamlObjectMapper;
    }

    @Override
    public ResponseEntity<LoggingLevel> getLoggingLevel(String logger) {
        Object loggerFrontEnd = LoggerFactory.getLogger(logger);
        if (loggerFrontEnd instanceof Logger) {
            Logger loggerBackEnd = (Logger) loggerFrontEnd;
            Level level = loggerBackEnd.getEffectiveLevel();
            LoggingLevel result = new LoggingLevel().levelInt(level.levelInt).levelStr(level.levelStr);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<Void> setLoggingLevel(String logger, String levelName) {
        Object loggerFrontEnd = LoggerFactory.getLogger(logger);
        if (loggerFrontEnd instanceof Logger) {
            log.info("Specified level: [{}]", levelName);
            Level level = Level.toLevel(levelName, Level.INFO);
            Logger loggerBackEnd = (Logger) loggerFrontEnd;
            loggerBackEnd.setLevel(level);
            log.info("New logging level: {}: {}({})", loggerBackEnd.getName(), level.levelStr, level.levelInt);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<Void> uploadLoggingLevels(MultipartFile data) {
        log.info("Upload logging levels YAML");
        try {
            uploadLoggingLevels(data.getInputStream());
            return ResponseEntity.ok().build();
        } catch (RuntimeException | IOException exception) {
            log.error("Exception while uploading logging levels YAML", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private void uploadLoggingLevels(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            StringBuilder builder = new StringBuilder();
            line = reader.readLine();
            if (line == null) {
                return;
            }
            if (!line.startsWith("%") && !line.equals("---")) {
                builder.append(line);
            }
            while ((line = reader.readLine()) != null) {
                if (line.equals("---")) {
                    String item = builder.toString();
                    builder = new StringBuilder();
                    updateLoggingLevels(item);
                } else {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(line);
                }
            }
            updateLoggingLevels(builder.toString());
        }
    }

    private void updateLoggingLevels(String item) {
        if (StringUtils.isEmpty(item)) {
            return;
        }
        Map<String,String> update = readValue(item);
        if (update == null) {
            return;
        }
        for (Map.Entry<String,String> entry : update.entrySet()) {
            String loggerName = entry.getKey();
            String levelName = entry.getValue();
            setLoggingLevel(loggerName, levelName);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String,String> readValue(String item) {
        try {
            return yamlObjectMapper.readValue(item, Map.class);
        } catch (Exception exception) {
            log.warn("Exception while importing logging levels: {}: {}", exception.toString(), String.valueOf(exception.getCause()));
            return null;
        }
    }
}
