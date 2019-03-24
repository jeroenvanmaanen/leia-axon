package org.leialearns.axon.domain.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.leialearns.axon.domain.command.CreateDomainCommandUnsafe;
import org.leialearns.axon.domain.command.DeclareClosedCommand;
import org.leialearns.axon.domain.command.DeclareOpenCommand;
import org.leialearns.axon.domain.event.ClosedEvent;
import org.leialearns.axon.domain.event.DomainCreatedEvent;
import org.leialearns.axon.domain.event.RemainsOpenEvent;
import org.leialearns.axon.model.node.command.GetOrCreateSymbolCommand;
import org.leialearns.axon.domain.event.SymbolCreatedEvent;
import org.leialearns.model.Symbol;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

@Aggregate
@Getter
@NoArgsConstructor
@Slf4j
public class Domain {

    @AggregateIdentifier
    private String id;

    private String key;
    private Map<String,Symbol> symbols = new HashMap<>();
    private AtomicInteger lastOrdinal = new AtomicInteger(0);
    private boolean open = true;
    private boolean decided = false;
    private Integer uniformDescriptionLength;

    @CommandHandler
    public Domain(CreateDomainCommandUnsafe command) {
        id = command.getId();
        DomainCreatedEvent.builder().id(id).key(command.getKey()).build().apply();
    }

    @EventSourcingHandler
    public void on(DomainCreatedEvent event) {
        id = event.getId();
        key = event.getKey();
    }

    @CommandHandler
    public Symbol handle(GetOrCreateSymbolCommand command) {
        String name = command.getName();
        return symbols.computeIfAbsent(command.getName(), n -> {
            if (!open) {
                throw new IllegalStateException(format("Domain is closed, no new symbols can be added: %s: %s", id, name));
            }
            Symbol symbol = new Symbol();
            symbol.setName(name);
            symbol.setOrdinal(lastOrdinal.incrementAndGet());
            setDescriptionLength(symbol);
            SymbolCreatedEvent.builder().symbol(symbol).build().apply();
            return symbol;
        });
    }

    @EventSourcingHandler
    public void on(SymbolCreatedEvent event) {
        Symbol symbol = event.getSymbol();
        lastOrdinal.updateAndGet(last -> Math.max(last, symbol.getOrdinal()));
        symbols.put(symbol.getName(), symbol);
    }

    @CommandHandler
    public void handle(DeclareClosedCommand command) {
        if (!decided) {
            ClosedEvent.builder().build().apply();
        } else if (!open) {
            log.warn("Domain was already closed: {}", id);
        } else {
            throw new IllegalStateException(format("Domain was declared to remain open: %s", id));
        }
    }

    @EventSourcingHandler
    public void on(ClosedEvent event) {
        decided = true;
        open = false;
        uniformDescriptionLength = BigInteger.valueOf(symbols.size()).bitLength();
        fixDescriptionLengths();
    }

    @CommandHandler
    public void handle(DeclareOpenCommand command) {
        if (!decided) {
            RemainsOpenEvent.builder().build().apply();
        } else if (open) {
            log.warn("Domain was already declared to remain open: {}", id);
        } else {
            throw new IllegalStateException(format("Domain was closed: %s", id));
        }
    }

    @EventSourcingHandler
    public void on(RemainsOpenEvent event) {
        decided = true;
        open = true;
        fixDescriptionLengths();
    }

    private void fixDescriptionLengths() {
        symbols.keySet().forEach(symbol -> symbols.put(symbol, setDescriptionLength(symbols.get(symbol))));
    }

    private Symbol setDescriptionLength(Symbol symbol) {
        if (decided) {
            int descriptionLength = open ? descriptionLength(symbol.getOrdinal()) : uniformDescriptionLength;
            symbol.setDescriptionLength(descriptionLength);
        }
        return symbol;
    }

    private int descriptionLength(int ordinal) {
        return (2 * BigInteger.valueOf(ordinal).bitLength()) + 1;
    }
}
