<div align="center">

# ✦ Tagify

**Modern name prefixes & suffixes for Paper — tab list, above the head and chat.**

The stable, actively maintained replacement for NametagEdit.

[![Build](https://github.com/Mathildeuh/Tagify/actions/workflows/build.yml/badge.svg)](https://github.com/Mathildeuh/Tagify/actions/workflows/build.yml)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)

</div>

---

## Why Tagify

The old NametagEdit is stagnant: server crashes with ItemsAdder icons in prefixes, broken
hex + formatting combinations, refreshing that no longer works with recent permission plugins,
a broken FlatFile → SQL migration. Tagify starts over on a modern base:

- **MiniMessage everywhere** — hex colours, gradients, formatting, with no overlap bugs.
- **Reliable, event-driven refresh** — a direct hook on LuckPerms recalculations, no polling.
- **Component sanitising** — ItemsAdder / Oraxen / Nexo icons in a prefix no longer crash the client.
- **Native Folia support** — region-aware scheduler, no blocking tasks.
- **Reliable storage migration** — `/tagify convert flatfile mysql` (and back), with no data loss.
- **Documented public API** — other plugins read and change tags from code.

## Editions

| | **Free** | **Premium** |
|---|---|---|
| Distribution | [SpigotMC](#) · [Modrinth](#) · [GitHub](https://github.com/Mathildeuh/Tagify) | [BuiltByBit](#) |
| Licence | Open source, GPL-3.0 | Proprietary |
| Storage | FlatFile (YAML) | + MySQL / MariaDB · PostgreSQL · MongoDB |
| Cross-server sync | — | Redis pub/sub, real time |
| Tab-list sorting | by priority | + explicit **weight** and **automatic** modes |
| Group conditions | — | per-world / per-permission gates, priority boosts |
| Admin GUI | simplified | full (weight control, colour & gradient presets) |
| NametagEdit import | ✅ | ✅ |
| PlaceholderAPI | base placeholders | extended placeholders |
| Support | GitHub Issues | priority |

Without the Premium module (or with an invalid licence), the plugin keeps running normally in
the Free edition — the advanced features are simply disabled, with no crash and no lockout.

## Compatibility

- **Server**: Paper 1.20.6 → 26.2+ (and forks: Purpur, Pufferfish, Folia). Spigot loads, Paper recommended.
- **Java**: 21+ (the bytecode targets Java 21).
- **Optional**: LuckPerms, PlaceholderAPI, Vault, ProtocolLib, ItemsAdder / Oraxen / Nexo.

## Installation

1. Drop `Tagify-x.y.z.jar` into `plugins/`.
2. Start the server, then edit `plugins/Tagify/config.yml` and `plugins/Tagify/lang/en.yml`.
3. `/tagify reload`.

Migrating from NametagEdit: keep the old plugin installed long enough to run
`/tagify import legacy`, then remove it.

## Commands

Base command: `/tagify` (aliases `/tgy`, `/ty`, `/tfy`).

| Command | Permission |
|---|---|
| `/tagify set <player> <prefix\|suffix> <value>` | `tagify.admin.set` |
| `/tagify remove <player> <prefix\|suffix>` | `tagify.admin.remove` |
| `/tagify info <player>` | `tagify.admin.info` |
| `/tagify group create\|delete\|list\|set\|priority\|weight\|condition\|addplayer\|removeplayer …` | `tagify.admin.group` |
| `/tagify gui [player]` | `tagify.admin.gui` |
| `/tagify reload` | `tagify.admin.reload` |
| `/tagify convert <source> <target>` | `tagify.admin.convert` |
| `/tagify import legacy` | `tagify.admin.import` |
| `/tagify refresh [player]` | `tagify.admin.refresh` |
| `/tagify version` · `/tagify help` | `tagify.use` |

Other permissions: `tagify.bypass.length` (exceed the character limit),
`tagify.group.<name>` (belong to a tag group), `tagify.*` (everything).

## Configuration

`config.yml` is commented and organised into sections (`storage`, `cache`, `refresh`, `display`,
`groups`, `integrations`, `gui`, `premium`, `advanced`). Messages live in `lang/en.yml` /
`lang/fr.yml`, entirely in MiniMessage.

## For developers

The API is included in the jar. As `compileOnly`:

```kotlin
repositories { maven("https://repo.papermc.io/repository/maven-public/") }
dependencies { compileOnly("fr.mathildeuh:tagify:1.0.0") } // or the local jar
```

```java
TagifyApi tagify = Tagify.get();
tagify.tags().resolve(uuid).thenAccept(tags ->
        tags.renderedPrefix().ifPresent(prefix -> /* ... */));
tagify.tags().setTemporaryTag(uuid, Tag.prefix("<gold>[Event] "));
```

Details and examples: [`docs/API.md`](docs/API.md).

## Building from source

```bash
./gradlew buildAll      # -> build/libs/Tagify-<version>.jar (+ Premium jar if the module is present)
./gradlew runServer     # local Paper test server
```

The version comes from Git tags (`vX.Y.Z`); the build number is the commit count.
The Premium module (`tagify-premium/`) is closed and absent from this repository: a public
clone only builds the Free edition.

## Licence

Free edition: **GPL-3.0** (see [`LICENSE`](LICENSE)). Any fork or reuse must stay open source
under the same licence. The Premium module is proprietary and distributed exclusively on BuiltByBit.
