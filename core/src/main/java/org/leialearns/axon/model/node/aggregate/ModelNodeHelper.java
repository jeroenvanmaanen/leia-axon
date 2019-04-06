package org.leialearns.axon.model.node.aggregate;

import org.leialearns.model.SymbolReference;
import org.springframework.stereotype.Component;

@Component
public class ModelNodeHelper {

    public String getKey(SymbolReference[] path) {
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
}
