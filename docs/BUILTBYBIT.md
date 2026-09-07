BuiltByBit resource description — paste as-is into the resource's BBCode editor.

═══════════════════════════════════════════════════════════════════════════════

[CENTER][SIZE=7][B]✦ Tagify Premium[/B][/SIZE]
[SIZE=4]Modern name prefixes & suffixes — tab list, above the head and chat.[/SIZE]

[SIZE=5][B]The stable, actively maintained replacement for NametagEdit.[/B][/SIZE]

[SIZE=3]This is the [B]Premium addon[/B] for [URL=#]Tagify[/URL] (free resource). Buying it unlocks the
Premium jar — see [I]Installation[/I] below for how it replaces the free one.[/SIZE][/CENTER]

[HR][/HR]

[SIZE=5][B]❯ Why switch[/B][/SIZE]

The old NametagEdit hasn't moved in years. The known bugs are still open:

[LIST]
[*][B]Server crash[/B] with ItemsAdder icons in prefixes
[*][B]Hex colours + formatting[/B] stepping on each other
[*][B]Broken refresh[/B] with recent versions of LuckPerms / tab plugins
[*][B]FlatFile → MySQL migration[/B] that corrupts data
[/LIST]

Tagify starts from a clean base, built for 2024+ servers — and this is the Premium edition:
a self-contained [ICODE]Tagify-Premium.jar[/ICODE] that replaces the Free jar outright, no
separate download needed.

[HR][/HR]

[SIZE=5][B]❯ What's in Premium[/B][/SIZE]

[LIST]
[*]🗄️ [B]MySQL / MariaDB · PostgreSQL · MongoDB[/B] storage, pooled with HikariCP — for
networks that outgrow FlatFile
[*]🔄 [B]Real-time cross-server sync[/B] via Redis pub/sub — a tag change on one server is
live on every other server instantly, no restart, no shared database polling
[*]📊 [B]Advanced tab-list sorting[/B] — explicit per-group [B]weight[/B], or an
[B]automatic[/B] mode with a configurable tie-breaker
[*]🧮 [B]Conditional group priorities[/B] — per-world / per-permission gates that boost or
override a group's priority for specific players
[*]🎛️ [B]Full admin GUI[/B] — weight control, colour & gradient presets, live preview
[*]🌍 [B]Extended PlaceholderAPI placeholders[/B]
[*]🎧 [B]Priority support[/B] on this page
[/LIST]

[SIZE=5][B]❯ Everything the Free edition already has[/B][/SIZE]

[LIST]
[*]🎨 [B]MiniMessage everywhere[/B] — hex, gradients, formatting, with no overlap bugs
[*]⚡ [B]Event-driven refresh[/B] — a direct LuckPerms hook, zero polling
[*]🛡️ [B]Component sanitising[/B] — ItemsAdder / Oraxen / Nexo icons no longer crash the client
[*]🧵 [B]Native Folia support[/B] — region-aware scheduler, no blocking tasks
[*]👥 [B]Permission-linked tag groups[/B] with priorities
[*]🔌 [B]Documented public API[/B] for other plugins
[*]📥 [B]Automatic import[/B] from NametagEdit — [ICODE]/tagify import legacy[/ICODE]
[/LIST]

If the licence check ever fails (network hiccup, expired key), Tagify never locks up or
crashes — it falls back to the Free feature set with a clear console warning, and picks
Premium back up automatically once the licence is valid again.

[HR][/HR]

[SIZE=5][B]❯ Compatibility[/B][/SIZE]

[LIST]
[*][B]Server[/B]: Paper 1.20.6 → 26.2+ (Purpur, Pufferfish, Folia). Spigot loads, Paper recommended.
[*][B]Java[/B]: 21+
[*][B]Optional[/B]: LuckPerms · PlaceholderAPI · Vault · ProtocolLib · ItemsAdder / Oraxen / Nexo
[/LIST]

[SIZE=5][B]❯ Installation[/B][/SIZE]

[ICODE]Tagify-Premium.jar[/ICODE] is a [B]drop-in replacement[/B] for the free [ICODE]Tagify.jar[/ICODE] —
it's the same plugin with the Premium module built in, not a second plugin running alongside it.
Only one jar is ever loaded.

[LIST=1]
[*][B]Remove[/B] [ICODE]Tagify.jar[/ICODE] (free edition) from [ICODE]plugins/[/ICODE] if it's there
[*]Drop [ICODE]Tagify-Premium.jar[/ICODE] into [ICODE]plugins/[/ICODE] and start the server once to generate the config
[*]Set your storage backend in [ICODE]config.yml[/ICODE] (or keep FlatFile) and, if you use MySQL/PostgreSQL/MongoDB/Redis, fill in the connection details
[*][ICODE]/tagify reload[/ICODE] — the licence is verified automatically against this purchase
[/LIST]

Your existing config, groups and player data carry over as-is — same file layout, same commands.

Migrating from NametagEdit: run [ICODE]/tagify import legacy[/ICODE]. Switching storage backend:
[ICODE]/tagify convert flatfile mysql[/ICODE] — no manual data editing needed.

[HR][/HR]

[SIZE=5][B]❯ Support[/B][/SIZE]

Open a thread on this resource page or use Discussion — I read every message. Please include
your server version, storage backend and console log when reporting an issue.

[CENTER][SIZE=3]The Free edition's code is open source (GPL-3.0) on [URL=https://github.com/Mathildeuh/Tagify]GitHub[/URL]. The Premium module is proprietary and sold exclusively here.[/SIZE][/CENTER]
