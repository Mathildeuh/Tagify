<!-- Modrinth description — Markdown. Paste into the project's Description tab. -->

# ✦ Tagify

**Modern name prefixes & suffixes for Paper / Folia** — applied at once to the tab list, the
nametag above the head and chat (via placeholder).

A full replacement for NametagEdit, written for today's servers: MiniMessage end to end,
event-driven refresh, native Folia support, and a clean API.

---

## Highlights

| | |
|---|---|
| **Text** | MiniMessage: hex, gradients, combined formatting with no artefacts. Automatic `&`/`§` legacy fallback. |
| **Threads** | Native Folia — region-aware scheduler, no I/O on the main thread. |
| **Permissions** | Event-driven LuckPerms hook (`UserDataRecalculateEvent`) — no polling. Also works with Bukkit permissions alone. |
| **Robustness** | Components sanitised before applying → ItemsAdder / Oraxen / Nexo icons in a prefix no longer kick the client. |
| **Storage** | FlatFile YAML (Free). MySQL / PostgreSQL / MongoDB + Redis sync (Premium). `/tagify convert` migration. |
| **API** | `Tagify.get()` → `tags()` / `groups()` services, events, PlaceholderAPI placeholders. |

## Compatibility

- Paper **1.20.6 → 26.2+**, forks included (Purpur, Pufferfish, **Folia**).
- Java 21+.
- Optional integrations: LuckPerms, PlaceholderAPI, Vault, ProtocolLib, ItemsAdder / Oraxen / Nexo.

## Getting started

```
/tagify set <player> prefix <gradient:#5B8DEF:#9B6BFF>[VIP]</gradient>
/tagify group create staff
/tagify group set staff prefix <red>[Staff]
/tagify group priority staff 100
/tagify group addplayer staff <player>
/tagify gui
```

Migrating from NametagEdit: `/tagify import legacy` (keep the old plugin around for the import).

## For developers

```java
TagifyApi tagify = Tagify.get();

tagify.tags().resolve(uuid).thenAccept(tags ->
        tags.renderedPrefix().ifPresent(component -> /* ... */));

tagify.tags().setTemporaryTag(uuid, Tag.prefix("<gold>[Event] ")); // not persisted
```

Full guide: [`docs/API.md`](https://github.com/Mathildeuh/Tagify/blob/main/docs/API.md).

## Editions

This page distributes the **Free edition** (open source, GPL-3.0). It is standalone and complete.

The **Premium edition** (SQL/Mongo storage, cross-server Redis sync, advanced tab-list sorting,
conditional group priorities, advanced GUI) is distributed on BuiltByBit. Without it the plugin
runs normally in the Free edition.

## Links

- Source & issues: [GitHub](https://github.com/Mathildeuh/Tagify)
- Premium edition: [BuiltByBit](#)

*Development builds: the "dev" channel of the [GitHub Releases](https://github.com/Mathildeuh/Tagify/releases).*
