package dk.madsravn.interpreter.object;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Environment {
    private Map<String, IObject> store;
    private Map<String, IObject> outerStore;

    public Environment() {
        store = new HashMap<String, IObject>();
        outerStore = new HashMap<String, IObject>();
    }

    public Environment(Map<String, IObject> outerStore) {
        store = new HashMap<String, IObject>();
        this.outerStore = outerStore;
    }

    public Optional<IObject> get(String name) {
        return Optional.ofNullable(
                Optional.ofNullable(store.get(name)).orElse(outerStore.get(name))
        );
    }

    public IObject set(String name, IObject value) {
        store.put(name, value);
        return value;
    }
}
