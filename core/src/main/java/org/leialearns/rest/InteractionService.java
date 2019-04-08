package org.leialearns.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

@Component
public class InteractionService implements InteractionApiDelegate {
    private static final Pattern LINE_PATTERN = Pattern.compile("^[<>!].*$");

    private final StateTrackerService stateTrackerService;
    private final ModelStructureService modelStructureService;

    public InteractionService(StateTrackerService stateTrackerService, ModelStructureService modelStructureService) {
        this.stateTrackerService = stateTrackerService;
        this.modelStructureService = modelStructureService;
    }

    @Override
    public ResponseEntity<Void> uploadInteraction(MultipartFile data) {
        int sleepMillis = 0;
        String currentStateId = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(data.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) !=  null) {
                line = line.trim();
                if (!LINE_PATTERN.matcher(line).matches()) {
                    continue;
                }
                if (line.startsWith("!markExtensible")) {
                    modelStructureService.markExtensible(currentStateId);
                    continue;
                }
                if (line.startsWith("!slow ")) {
                    sleepMillis = Integer.parseInt(line.replaceAll("^[^ ]* ", ""));
                }
                String[] parts = line.substring(1).split(" *: *", 2);
                if (parts.length < 2) {
                    continue;
                }
                String vocabulary = parts[0];
                String symbol = parts[1];
                boolean isPerception = line.charAt(0) == '<';
                if (isPerception) {
                    stateTrackerService.recordSymbol(currentStateId, vocabulary, symbol);
                }
                currentStateId = stateTrackerService.advance(currentStateId, vocabulary, symbol);
                if (sleepMillis > 0) {
                    Thread.sleep(sleepMillis);
                }
            }
        } catch (Exception exception) {
            log.warn("Exception while uploading interaction", exception);
        }
        return null;
    }
}
