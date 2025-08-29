package io.github.fishstiz.cursors_extended.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class Alias<T> {
    private final Map<T, Key> aliases;

    public Alias(int initialCapacity, float loadFactor) {
        this.aliases = new Object2ObjectOpenHashMap<>(initialCapacity, loadFactor);
    }

    public Key addKey(T alias) {
        return this.aliases.computeIfAbsent(alias, Key::new);
    }

    /**
     * Retroactively add keys in case a mod creates a standard cursor before it's registered.
     */
    public void addAlias(T original, T alias) {
        this.aliases.putIfAbsent(alias, this.aliases.computeIfAbsent(original, Key::new));
    }

    public Key lookup(T alias) {
        return this.aliases.get(alias);
    }

    public class Key {
        private final T id;

        private Key(T id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return this.id.toString();
        }
    }
}
