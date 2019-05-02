package org.leialearns.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.leialearns.model.LoggingLevel;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class LoggingService implements LoggingApiDelegate {

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
            Level level = Level.toLevel(levelName, Level.INFO);
            Logger loggerBackEnd = (Logger) loggerFrontEnd;
            loggerBackEnd.setLevel(level);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
