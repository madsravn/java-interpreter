package dk.madsravn.interpreter.object;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Environment {
    private Map<String, IObject> store;
    // TODO: Is Optional the correct solution here?
    private Optional<Environment> outerStore;

    public Environment() {
        store = new HashMap<String, IObject>();
        outerStore = Optional.empty();
    }

    public Environment(Environment outerStore) {
        store = new HashMap<String, IObject>();
        this.outerStore = Optional.of(outerStore);
    }

    //TODO: UGLY AS HELL
    public Optional<IObject> get(String name) {
        //return Optional.ofNullable(store.get(name)).orElse(Optional.ofNullable(outerStore.get(name).get()));
        var first = store.get(name);
        if(first != null) {
            return Optional.ofNullable(first);
        } else {
            if(outerStore.isPresent()) {
                var second = outerStore.get().get(name);
                if (second.isPresent()) {
                    return second;
                }
            }
        }
        return Optional.empty();
    }

    public IObject set(String name, IObject value) {
        store.put(name, value);
        return value;
    }
}
