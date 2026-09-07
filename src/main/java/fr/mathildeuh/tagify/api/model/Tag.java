package fr.mathildeuh.tagify.api.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Couple immuable préfixe / suffixe, sous leur forme brute (chaîne MiniMessage ou legacy
 * telle que saisie / stockée). Le rendu en {@link net.kyori.adventure.text.Component} est
 * réalisé par le {@link fr.mathildeuh.tagify.api.service.TagService}.
 *
 * <p>Une valeur {@code null} ou vide signifie « pas de préfixe / suffixe défini ».
 *
 * @param prefix préfixe brut, ou {@code null}
 * @param suffix suffixe brut, ou {@code null}
 */
public record Tag(@Nullable String prefix, @Nullable String suffix) {

    /** Tag sans préfixe ni suffixe. */
    public static final Tag EMPTY = new Tag(null, null);

    public Tag {
        prefix = emptyToNull(prefix);
        suffix = emptyToNull(suffix);
    }

    @Contract(pure = true)
    public static Tag of(@Nullable String prefix, @Nullable String suffix) {
        return new Tag(prefix, suffix);
    }

    @Contract(pure = true)
    public static Tag prefix(@Nullable String prefix) {
        return new Tag(prefix, null);
    }

    @Contract(pure = true)
    public static Tag suffix(@Nullable String suffix) {
        return new Tag(null, suffix);
    }

    public boolean isEmpty() {
        return prefix == null && suffix == null;
    }

    public boolean hasPrefix() {
        return prefix != null;
    }

    public boolean hasSuffix() {
        return suffix != null;
    }

    public Optional<String> prefixOptional() {
        return Optional.ofNullable(prefix);
    }

    public Optional<String> suffixOptional() {
        return Optional.ofNullable(suffix);
    }

    @Contract(pure = true)
    public Tag withPrefix(@Nullable String newPrefix) {
        return new Tag(newPrefix, suffix);
    }

    @Contract(pure = true)
    public Tag withSuffix(@Nullable String newSuffix) {
        return new Tag(prefix, newSuffix);
    }

    /**
     * Fusionne ce tag avec un autre : chaque champ absent ici est comblé par {@code fallback}.
     */
    @Contract(pure = true)
    public Tag merge(Tag fallback) {
        return new Tag(prefix != null ? prefix : fallback.prefix,
                suffix != null ? suffix : fallback.suffix);
    }

    private static @Nullable String emptyToNull(@Nullable String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}
