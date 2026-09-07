package fr.mathildeuh.tagify.internal.storage;

import fr.mathildeuh.tagify.api.model.Tag;
import fr.mathildeuh.tagify.api.model.TagGroup;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/** YAML serialization for a {@link TagGroup} (used by the FlatFile backend). */
public final class GroupCodec {

    private GroupCodec() {
    }

    public static void write(ConfigurationSection section, TagGroup group) {
        // No separate "name" field: it's always derivable as the section key (group.key() is
        // name.toLowerCase()), so persisting both would just duplicate the same information.
        section.set("prefix", group.tag().prefix());
        section.set("suffix", group.tag().suffix());
        section.set("priority", group.priority());
        section.set("weight", group.weight() == 0 ? null : group.weight());

        List<String> members = new ArrayList<>();
        for (UUID member : group.members()) {
            members.add(member.toString());
        }
        section.set("members", members);

        section.set("metadata", null);
        if (!group.metadata().isEmpty()) {
            ConfigurationSection meta = section.createSection("metadata");
            group.metadata().forEach(meta::set);
        }
    }

    public static TagGroup read(ConfigurationSection section, String key, Logger logger) {
        // "name" is read only for backward compatibility with files written before this field
        // was dropped; new writes never set it, so this simply falls back to the section key.
        String name = section.getString("name", key);
        TagGroup.Builder builder = TagGroup.builder(name)
                .tag(Tag.of(section.getString("prefix"), section.getString("suffix")))
                .priority(section.getInt("priority", 0))
                .weight(section.getInt("weight", 0));

        for (String raw : section.getStringList("members")) {
            try {
                builder.addMember(UUID.fromString(raw));
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid member UUID in group '" + key + "': " + raw);
            }
        }

        ConfigurationSection meta = section.getConfigurationSection("metadata");
        if (meta != null) {
            Map<String, String> map = new LinkedHashMap<>();
            for (String metaKey : meta.getKeys(false)) {
                map.put(metaKey, meta.getString(metaKey));
            }
            builder.metadata(map);
        }

        return builder.build();
    }
}
