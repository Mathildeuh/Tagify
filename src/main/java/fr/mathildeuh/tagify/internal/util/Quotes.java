package fr.mathildeuh.tagify.internal.util;

/**
 * Optional single-quote unwrapping for free-text command values.
 *
 * <p>Bukkit's command parser does not do shell-style quoting: raw args are split on
 * whitespace and nothing strips or interprets {@code '}. That collapses leading/trailing
 * spaces in values (Minecraft chat also trims the line), so a value that must start or end
 * with a space — a prefix like {@code [VIP] } — cannot be typed directly. Wrapping it in a
 * matching pair of single quotes, e.g. {@code '[VIP] '}, opts into unwrapping: the outer
 * quotes are removed and the content, including any leading/trailing whitespace, is kept as
 * typed. An escaped quote ({@code \'}) inside the wrapped value is kept as a literal {@code '}
 * rather than closing the value early.
 */
public final class Quotes {

    private Quotes() {
    }

    /**
     * If {@code raw} starts and ends with an (unescaped) {@code '}, returns its content with
     * {@code \'} un-escaped to {@code '}. Otherwise returns {@code raw} unchanged.
     */
    public static String unwrap(String raw) {
        if (raw == null || raw.length() < 2
                || raw.charAt(0) != '\'' || raw.charAt(raw.length() - 1) != '\''
                || isEscaped(raw, raw.length() - 1)) {
            return raw;
        }
        return raw.substring(1, raw.length() - 1).replace("\\'", "'");
    }

    /** {@code true} if the character at {@code index} is preceded by an odd run of backslashes. */
    private static boolean isEscaped(String s, int index) {
        int backslashes = 0;
        int i = index - 1;
        while (i >= 0 && s.charAt(i) == '\\') {
            backslashes++;
            i--;
        }
        return backslashes % 2 == 1;
    }
}
