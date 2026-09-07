package fr.mathildeuh.tagify.internal.storage;

import java.util.Locale;
import java.util.Optional;

/** Backends de stockage connus. FlatFile est le seul disponible en édition Free. */
public enum StorageType {

    FLATFILE(false),
    MYSQL(true),
    POSTGRESQL(true),
    MONGODB(true);

    private final boolean premium;

    StorageType(boolean premium) {
        this.premium = premium;
    }

    /** {@code true} si ce backend nécessite le module Premium. */
    public boolean requiresPremium() {
        return premium;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<StorageType> fromId(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
