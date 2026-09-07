package fr.mathildeuh.tagify.util;

import fr.mathildeuh.tagify.internal.util.Quotes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuotesTest {

    @Test
    void unwrapsMatchingQuotesAndKeepsTrailingSpace() {
        assertEquals("[VIP] ", Quotes.unwrap("'[VIP] '"));
    }

    @Test
    void unwrapsMatchingQuotesAndKeepsLeadingSpace() {
        assertEquals(" [VIP]", Quotes.unwrap("' [VIP]'"));
    }

    @Test
    void unescapesInnerQuote() {
        assertEquals("It's a [VIP] ", Quotes.unwrap("'It\\'s a [VIP] '"));
    }

    @Test
    void leavesUnquotedValueUnchanged() {
        assertEquals("<red>[VIP] ", Quotes.unwrap("<red>[VIP] "));
    }

    @Test
    void leavesLoneQuoteCharacterUnchanged() {
        assertEquals("'", Quotes.unwrap("'"));
    }

    @Test
    void leavesUnmatchedOpeningQuoteUnchanged() {
        assertEquals("'[VIP]", Quotes.unwrap("'[VIP]"));
    }

    @Test
    void doesNotUnwrapWhenClosingQuoteIsEscaped() {
        assertEquals("'[VIP]\\'", Quotes.unwrap("'[VIP]\\'"));
    }

    @Test
    void handlesNullAndEmpty() {
        assertNull(Quotes.unwrap(null));
        assertEquals("", Quotes.unwrap(""));
    }
}
