package fr.mathildeuh.tagify.internal.storage;

import fr.mathildeuh.tagify.internal.storage.flatfile.FlatFileDataStore;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Registre des fabriques de backends. Le Core enregistre {@link StorageType#FLATFILE} ;
 * le module Premium enregistre MySQL / PostgreSQL / MongoDB via {@code ModuleContext}.
 */
public final class StorageRegistry {

    private final Map<StorageType, Function<StorageContext, DataStore>> factories =
            new EnumMap<>(StorageType.class);

    public StorageRegistry() {
        register(StorageType.FLATFILE, ctx -> new FlatFileDataStore(ctx.plugin()));
    }

    public void register(StorageType type, Function<StorageContext, DataStore> factory) {
        factories.put(type, factory);
    }

    public boolean isRegistered(StorageType type) {
        return factories.containsKey(type);
    }

    public Optional<DataStore> create(StorageType type, StorageContext context) {
        Function<StorageContext, DataStore> factory = factories.get(type);
        return factory == null ? Optional.empty() : Optional.of(factory.apply(context));
    }
}
