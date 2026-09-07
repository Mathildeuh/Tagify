package fr.mathildeuh.tagify.api.model;

/**
 * Origine d'un préfixe ou d'un suffixe effectif d'un joueur.
 *
 * <p>Ordre de précédence (du plus fort au plus faible) :
 * {@link #API} &gt; {@link #INDIVIDUAL} &gt; {@link #GROUP} &gt; {@link #NONE}.
 */
public enum TagSource {

    /** Aucun tag défini. */
    NONE,

    /** Tag temporaire posé par un autre plugin via l'API (non persisté). */
    API,

    /** Tag défini individuellement pour le joueur (persisté). */
    INDIVIDUAL,

    /** Tag hérité d'un groupe (via priorité). */
    GROUP
}
