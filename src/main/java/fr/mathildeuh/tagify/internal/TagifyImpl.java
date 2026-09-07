package fr.mathildeuh.tagify.internal;

import fr.mathildeuh.tagify.api.TagifyApi;
import fr.mathildeuh.tagify.api.service.GroupService;
import fr.mathildeuh.tagify.api.service.TagService;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Implémentation de l'{@link TagifyApi} enregistrée dans le {@code ServicesManager}. */
public final class TagifyImpl implements TagifyApi {

    private final TagService tags;
    private final GroupService groups;
    private final Supplier<String> backend;
    private final BooleanSupplier premium;

    public TagifyImpl(TagService tags, GroupService groups,
                      Supplier<String> backend, BooleanSupplier premium) {
        this.tags = tags;
        this.groups = groups;
        this.backend = backend;
        this.premium = premium;
    }

    @Override
    public TagService tags() {
        return tags;
    }

    @Override
    public GroupService groups() {
        return groups;
    }

    @Override
    public String version() {
        return BuildConstants.fullVersion();
    }

    @Override
    public boolean isPremium() {
        return premium.getAsBoolean();
    }

    @Override
    public String storageBackend() {
        return backend.get();
    }
}
