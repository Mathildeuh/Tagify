package fr.mathildeuh.tagify.internal.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;

/** Utilitaires de manipulation de {@link Component}. */
public final class Components {

    private Components() {
    }

    /** Longueur du texte brut (formatage et couleurs retirés). */
    public static int plainLength(@Nullable Component component) {
        if (component == null) {
            return 0;
        }
        return PlainTextComponentSerializer.plainText().serialize(component).length();
    }

    public static String plain(@Nullable Component component) {
        return component == null ? "" : PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static boolean isEmpty(@Nullable Component component) {
        return component == null || plain(component).isEmpty();
    }

    /**
     * Dernière couleur explicite rencontrée dans l'arbre du composant (parcours profondeur
     * d'abord, la plus à droite l'emporte), ramenée à la couleur nommée la plus proche.
     * Sert à donner au pseudo la couleur de fin du préfixe.
     */
    public static @Nullable NamedTextColor trailingColor(@Nullable Component component) {
        if (component == null) {
            return null;
        }
        NamedTextColor[] holder = {null};
        walk(component, holder);
        return holder[0];
    }

    private static void walk(Component component, NamedTextColor[] holder) {
        TextColor color = component.color();
        if (color != null) {
            holder[0] = color instanceof NamedTextColor named ? named : NamedTextColor.nearestTo(color);
        }
        for (Component child : component.children()) {
            walk(child, holder);
        }
    }
}
