package fr.mathildeuh.tagify.tag;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import fr.mathildeuh.tagify.api.model.TagSource;
import fr.mathildeuh.tagify.internal.tag.TagComputer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TagComputerTest {

    private final TagComputer computer = new TagComputer();

    private static TagGroup group(String name, int priority, String prefix, String suffix) {
        return TagGroup.builder(name).priority(priority).tag(Tag.of(prefix, suffix)).build();
    }

    @Test
    void individualBeatsGroup() {
        var result = computer.compute(
                Tag.prefix("<red>[Ind] "),
                Tag.EMPTY,
                List.of(group("staff", 100, "<blue>[Staff] ", "<gray> *")));

        assertEquals("<red>[Ind] ", result.prefix());
        assertSame(TagSource.INDIVIDUAL, result.prefixSource());
        // Le suffixe n'ayant pas de valeur individuelle, il vient du groupe.
        assertEquals("<gray> *", result.suffix());
        assertSame(TagSource.GROUP, result.suffixSource());
    }

    @Test
    void temporaryBeatsIndividual() {
        var result = computer.compute(
                Tag.prefix("<red>[Ind] "),
                Tag.prefix("<gold>[Temp] "),
                List.of());

        assertEquals("<gold>[Temp] ", result.prefix());
        assertSame(TagSource.API, result.prefixSource());
    }

    @Test
    void highestPriorityGroupWins() {
        var high = group("admin", 500, "<dark_red>[Admin] ", null);
        var low = group("member", 10, "<green>[Member] ", null);

        var result = computer.compute(Tag.EMPTY, Tag.EMPTY, List.of(high, low));

        assertEquals("<dark_red>[Admin] ", result.prefix());
        assertEquals("admin", result.prefixGroup().name());
        assertEquals(500, result.priority());
    }

    @Test
    void fallsThroughToLowerGroupWhenHigherHasNoPrefix() {
        var high = group("vip", 200, null, "<aqua> ✦");
        var low = group("default", 0, "<gray>", null);

        var result = computer.compute(Tag.EMPTY, Tag.EMPTY, List.of(high, low));

        assertEquals("<gray>", result.prefix());
        assertEquals("default", result.prefixGroup().name());
        assertEquals("<aqua> ✦", result.suffix());
        // La priorité de tri reste celle du groupe le plus fort.
        assertEquals(200, result.priority());
    }

    @Test
    void emptyWhenNothingApplies() {
        var result = computer.compute(Tag.EMPTY, Tag.EMPTY, List.of());

        assertNull(result.prefix());
        assertNull(result.suffix());
        assertSame(TagSource.NONE, result.prefixSource());
        assertEquals(0, result.priority());
    }
}
