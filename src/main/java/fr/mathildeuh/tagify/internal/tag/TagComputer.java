package fr.mathildeuh.tagify.internal.tag;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.api.model.TagSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Applique les règles de précédence pour déterminer le préfixe / suffixe effectifs d'un joueur.
 *
 * <p>Précédence, champ par champ (préfixe et suffixe résolus indépendamment) :
 * <b>tag temporaire (API)</b> &gt; <b>tag individuel</b> &gt; <b>groupe de plus haute priorité
 * fournissant ce champ</b> &gt; aucun.
 *
 * <p>La priorité de tri (tab list) est celle du groupe de plus haute priorité du joueur,
 * indépendamment de la source du tag affiché.
 */
public final class TagComputer {

    public record Resolution(
            @Nullable String prefix, TagSource prefixSource, @Nullable TagGroup prefixGroup,
            @Nullable String suffix, TagSource suffixSource, @Nullable TagGroup suffixGroup,
            int priority) {

        public boolean isEmpty() {
            return prefix == null && suffix == null;
        }
    }

    /**
     * @param individual tag individuel persisté, ou {@link Tag#EMPTY}
     * @param temporary  tag temporaire API, ou {@link Tag#EMPTY}
     * @param groups     groupes du joueur, <b>déjà triés</b> par priorité décroissante puis nom croissant
     */
    public Resolution compute(Tag individual, Tag temporary, List<TagGroup> groups) {
        String prefix = null;
        TagSource prefixSource = TagSource.NONE;
        TagGroup prefixGroup = null;

        String suffix = null;
        TagSource suffixSource = TagSource.NONE;
        TagGroup suffixGroup = null;

        if (temporary.hasPrefix()) {
            prefix = temporary.prefix();
            prefixSource = TagSource.API;
        } else if (individual.hasPrefix()) {
            prefix = individual.prefix();
            prefixSource = TagSource.INDIVIDUAL;
        } else {
            for (TagGroup group : groups) {
                if (group.tag().hasPrefix()) {
                    prefix = group.tag().prefix();
                    prefixSource = TagSource.GROUP;
                    prefixGroup = group;
                    break;
                }
            }
        }

        if (temporary.hasSuffix()) {
            suffix = temporary.suffix();
            suffixSource = TagSource.API;
        } else if (individual.hasSuffix()) {
            suffix = individual.suffix();
            suffixSource = TagSource.INDIVIDUAL;
        } else {
            for (TagGroup group : groups) {
                if (group.tag().hasSuffix()) {
                    suffix = group.tag().suffix();
                    suffixSource = TagSource.GROUP;
                    suffixGroup = group;
                    break;
                }
            }
        }

        int priority = groups.isEmpty() ? 0 : groups.get(0).priority();

        return new Resolution(prefix, prefixSource, prefixGroup,
                suffix, suffixSource, suffixGroup, priority);
    }
}
