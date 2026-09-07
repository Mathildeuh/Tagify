# Tagify API — developer guide

The public API is included in the Tagify jar (package `fr.mathildeuh.tagify.api`).
It is stable; classes outside that package are internal and may change without notice.

## Dependency

```kotlin
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}
dependencies {
    compileOnly("fr.mathildeuh:tagify:1.0.0")
    // or, while nothing is published to a Maven repo:
    // compileOnly(files("libs/Tagify.jar"))
}
```

`plugin.yml`:

```yaml
softdepend: [Tagify]
```

## Access

Tagify registers itself in Bukkit's `ServicesManager`.

```java
import fr.mathildeuh.tagify.api.Tagify;
import fr.mathildeuh.tagify.api.TagifyApi;

TagifyApi tagify = Tagify.get();                 // throws IllegalStateException if absent
Optional<TagifyApi> maybe = Tagify.getIfLoaded();
```

## Reading a player's tags

```java
import fr.mathildeuh.tagify.api.model.PlayerTags;

tagify.tags().resolve(playerUuid).thenAccept((PlayerTags tags) -> {
    tags.renderedPrefix().ifPresent(component -> { /* Adventure Component */ });
    tags.prefix().ifPresent(raw -> { /* raw MiniMessage string */ });

    tags.prefixSource();   // NONE | API | INDIVIDUAL | GROUP
    tags.primaryGroup().ifPresent(group -> group.priority());
});

// Synchronous variant (cache only, may be empty):
Optional<PlayerTags> cached = tagify.tags().resolveCached(playerUuid);
```

## Changing tags

```java
import fr.mathildeuh.tagify.api.model.Tag;

// Persisted individual tag
tagify.tags().setIndividualPrefix(uuid, "<gradient:#5B8DEF:#9B6BFF>[VIP]</gradient> ");
tagify.tags().setIndividualTag(uuid, Tag.of("<red>[A] ", "<gray> *"));
tagify.tags().clearIndividual(uuid);

// Temporary tag (not persisted, highest precedence, cleared on disconnect)
tagify.tags().setTemporaryTag(uuid, Tag.prefix("<gold>[Event] "));
tagify.tags().setTemporaryTag(uuid, null); // remove

// Force a display refresh
tagify.tags().refresh(uuid);
```

## Groups

```java
import fr.mathildeuh.tagify.api.model.TagGroup;

tagify.groups().getGroups();
tagify.groups().getGroup("staff").ifPresent(g -> g.priority());
tagify.groups().getGroupsOf(uuid);          // effective groups, sorted by descending priority

tagify.groups().createGroup("vip")
    .thenCompose(g -> tagify.groups().setGroupTag("vip", Tag.prefix("<aqua>[VIP] ")))
    .thenCompose(v -> tagify.groups().setGroupPriority("vip", 50))
    .thenCompose(v -> tagify.groups().setGroupWeight("vip", 120)); // Premium tab-list sorting

tagify.groups().addMember("vip", uuid);
```

## Events

```java
import fr.mathildeuh.tagify.api.event.*;

@EventHandler
public void onTagUpdate(PlayerTagUpdateEvent event) {
    event.getPlayer();
    event.getTags();           // resolved PlayerTags, already applied
}

@EventHandler
public void onPreApply(PreTagApplyEvent event) {
    // Fired right before the scoreboard is updated, on the global thread.
    event.setPrefix(event.getPrefix());   // mutate the final Component
    event.setCancelled(true);             // or skip applying entirely
}

@EventHandler
public void onGroup(TagGroupUpdateEvent event) {
    event.getAction();   // CREATE | MODIFY | DELETE
    event.getGroup();    // null on DELETE
}
```

## Chat integration

Tagify does not force a chat format. To include it in a chat plugin, use PlaceholderAPI:

```
%tagify_prefix%        rendered prefix (§ + hex codes)
%tagify_prefix_mini%   prefix as MiniMessage
%tagify_prefix_plain%  prefix without formatting
%tagify_suffix% / _mini / _plain
%tagify_group%             primary group name
%tagify_group_priority%    primary group priority
%tagify_has_tag%           true / false
```

**Premium** adds: `%tagify_weight%`, `%tagify_groups%` (comma list), `%tagify_in_group_<name>%`,
`%tagify_prefix_amp%` / `%tagify_suffix_amp%` (legacy with `&`), `%tagify_nametag_mini%`.

Or, from code, `tagify.tags().renderPrefix(uuid)` / `renderSuffix(uuid)` (`Optional<Component>`).

## Notes

- `CompletableFuture` methods complete on an arbitrary thread — hop back onto the server /
  region thread before touching the Bukkit API.
- `resolveCached` returns empty if the player was never loaded (offline, never resolved).
