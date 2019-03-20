package org.sollunae.stack.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
@Slf4j
public class STACKService implements StackApiDelegate {

    @PostConstruct
    public void postConstruct() {
        log.info("Post construct: {}", getClass());
    }

    @Override
    public ResponseEntity<String> prod(String name) {
        return ResponseEntity.of(Optional.of("Hello, " + name + "!"));
    }
}
