package org.leialearns.axon.model.node.aggregate;

import org.leialearns.model.SymbolReference;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ModelNodeHelper {

    public String getKey(SymbolReference[] path) {
        return getKey(Arrays.asList(path));
    }

    public String getKey(Iterable<SymbolReference> path) {
        StringBuilder result = new StringBuilder("/");
        for (SymbolReference symbolReference : path) {
            result.append(keyProtect(symbolReference.getVocabulary()));
            result.append(":");
            result.append(symbolReference.getOrdinal());
            result.append("/");
        }
        return result.toString();
    }

    private String keyProtect(String part) {
        return part.replaceAll("%", "%25").replaceAll("/", "%2F").replaceAll(":", "%3A");
    }

    public String show(SymbolReference symbol) {
        if (symbol == null) {
            return "???";
        }
        return showObject(symbol.getVocabulary()) + ":" + showObject(symbol.getOrdinal());
    }

    private String showObject(Object object) {
        return object == null ? "?" : String.valueOf(object);
    }
}
