package fr.mathildeuh.tagify.internal.command;

import java.util.List;

/** Une sous-commande de {@code /tagify}. */
public interface SubCommand {

    String name();

    default List<String> aliases() {
        return List.of();
    }

    String permission();

    default boolean playerOnly() {
        return false;
    }

    /** Arguments affichés dans l'aide, après {@code /tagify <nom>} (ex. {@code <joueur> <prefix|suffix> <valeur>}). */
    String usageArgs();

    /** Clé de langue de la description (ex. {@code command.set.description}). */
    String descriptionKey();

    void run(CommandContext ctx);

    default List<String> tabComplete(CommandContext ctx) {
        return List.of();
    }
}
