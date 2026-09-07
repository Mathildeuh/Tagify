# Manual test checklist

Automated coverage: `./gradlew test` (tag resolution logic).
Everything else needs a real server — `./gradlew runServer` starts a local Paper 1.21.x.
`./gradlew runPremium` does the same with the Premium jar (set `premium.verify-mode: dev`).

## Version matrix

| Bracket | Check |
|---|---|
| Paper 1.20.6 | floor — loads, `api-version` accepted |
| Paper 1.21.x | reference — via `runServer` |
| Paper 26.x | head — loads, no `NoSuchMethodError` |
| Folia (latest) | `runFolia` or a manual Folia server — tags applied, no thread errors |

## Functional — Free edition

- [ ] `/tagify version` shows version + build + branch + platform + backend + edition.
- [ ] `/tagify set <me> prefix <gradient:#5B8DEF:#9B6BFF>[VIP]</gradient> ` → prefix visible in the tab list **and** above the head.
- [ ] `/tagify set <me> suffix <gray> ✦` → suffix visible in both places.
- [ ] `/tagify remove <me> prefix` → prefix removed, suffix kept.
- [ ] `/tagify info <me>` → shows the values and their origin (`individual`).
- [ ] `/tagify group create staff` + `group priority staff 100` + `group set staff prefix &c[Staff] ` + `group addplayer staff <me>` → `[Staff]` prefix applied.
- [ ] Two groups, different priorities, player in both → the higher one wins; the tab list is ordered accordingly.
- [ ] `group set staff prefix ` (empty) then a lower group with a prefix → the prefix drops to the lower group, the sort priority stays that of `staff`.
- [ ] `group delete staff` while a member is online → no error, the player loses the tag immediately.
- [ ] `/tagify set <offline player> prefix …` (player has joined before) → OK; player never joined → "player not found".
- [ ] Combined hex + gradient + bold + italic in a single prefix → renders correctly, no stray reset.
- [ ] Prefix containing an ItemsAdder / Oraxen / Nexo icon → **no crash / client kick** (with `sanitize-third-party-icons: true`).
- [ ] Length limit: prefix > `max-prefix-length` → rejected; with `tagify.bypass.length` → accepted.
- [ ] `/tagify gui` → main menu → Groups → edit prefix (chat input) → priority +/- → back.
- [ ] `/tagify reload` after editing `config.yml` / `lang/en.yml` → picked up.
- [ ] `/tagify refresh` and `/tagify refresh <player>`.

## Integrations

- [ ] With **LuckPerms**: changing an online player's LP group → tag refreshed automatically (no manual `/tagify refresh`).
- [ ] `groups.map-luckperms-groups: true` + a Tagify group named like an LP group → membership inherited.
- [ ] With **PlaceholderAPI**: `%tagify_prefix%`, `%tagify_prefix_mini%`, `%tagify_group%`, `%tagify_has_tag%` return the right values.
- [ ] A PAPI placeholder **inside** a prefix (e.g. `%player_name%`) → resolved before the MiniMessage parse.
- [ ] No integration at all → basic behaviour via Bukkit permissions (`tagify.group.<x>`).

## Premium edition

- [ ] `verify-mode: dev` + `storage.type: mysql` against a local MySQL → starts on flatfile then switches to `mysql` after validation ("Storage switched..." log).
- [ ] `storage.type: postgresql` → schema created, upserts work (the `ON CONFLICT` dialect).
- [ ] `storage.type: mongodb` → the `tagify_players` / `tagify_groups` collections are populated.
- [ ] `/tagify convert flatfile mysql` → report "X group(s), Y player(s)", data present in the database.
- [ ] `cache.redis.enabled: true` + 2 servers sharing the database: `set` on server A → tag updated on server B in < 1s.
- [ ] Redis cut mid-run → warning log, automatic reconnection, no blocking error.
- [ ] `tablist-sort.mode: weight` + `/tagify group weight <g> <n>` → tab list ordered by weight.
- [ ] `tablist-sort.mode: automatic` + `automatic-tiebreaker: luckperms-weight` → ordered by priority band then LP weight.
- [ ] `/tagify group condition <g> worlds world_nether` → the group's tag only shows in the nether.
- [ ] `/tagify group condition <g> priority-boost-permission vip.plus` + `priority-boost 100` → priority boosted when the player has the permission.
- [ ] `%tagify_weight%`, `%tagify_groups%`, `%tagify_in_group_<name>%` return the right values.
- [ ] Advanced GUI: group editor shows weight +/- and the "Colours & gradients" preset picker.
- [ ] Missing BuiltByBit token → Premium module cleanly disabled, plugin in Free mode, **no stack trace**.
- [ ] Jar obtained outside BuiltByBit (placeholders not substituted) → same, clear message.
- [ ] BuiltByBit API unreachable after a recent successful check → grace period respected.

## NametagEdit import

- [ ] `plugins/NametagEdit/groups.yml` + `players.yml` present → `/tagify import legacy` → groups and tags imported, priorities inverted correctly.
- [ ] Folder missing → "not found" message, no error.

## Robustness

- [ ] Full server `/reload` → Tagify re-enables cleanly, re-applies to online players.
- [ ] Server stop → flatfile saved, SQL / Redis pool closed, no orphan tasks.
- [ ] Another plugin also setting a scoreboard → coexistence (`scoreboard: dedicated` mode by default).
