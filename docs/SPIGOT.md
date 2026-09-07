SpigotMC description — paste as-is into the resource's BBCode editor.

═══════════════════════════════════════════════════════════════════════════════

[CENTER][SIZE=7][B]✦ Tagify[/B][/SIZE]
[SIZE=4]Modern name prefixes & suffixes — tab list, above the head and chat.[/SIZE]

[SIZE=5][B]The stable, actively maintained replacement for NametagEdit.[/B][/SIZE][/CENTER]

[HR][/HR]

[SIZE=5][B]❯ Why switch[/B][/SIZE]

The old NametagEdit hasn't moved in years. The known bugs are still open:

[LIST]
[*][B]Server crash[/B] with ItemsAdder icons in prefixes
[*][B]Hex colours + formatting[/B] stepping on each other
[*][B]Broken refresh[/B] with recent versions of LuckPerms / tab plugins
[*][B]FlatFile → MySQL migration[/B] that corrupts data
[/LIST]

Tagify starts from a clean base, built for 2024+ servers.

[HR][/HR]

[SIZE=5][B]❯ Features[/B][/SIZE]

[LIST]
[*]🎨 [B]MiniMessage everywhere[/B] — hex, gradients, formatting, with no overlap bugs
[*]⚡ [B]Event-driven refresh[/B] — a direct LuckPerms hook, zero polling
[*]🛡️ [B]Component sanitising[/B] — ItemsAdder / Oraxen / Nexo icons no longer crash the client
[*]🧵 [B]Native Folia support[/B] — region-aware scheduler, no blocking tasks
[*]👥 [B]Permission-linked tag groups[/B] with priorities
[*]🖥️ [B]In-game admin GUI[/B] (not just YAML)
[*]🔌 [B]Documented public API[/B] for other plugins
[*]📥 [B]Automatic import[/B] from NametagEdit — [ICODE]/tagify import legacy[/ICODE]
[*]🌍 [B]PlaceholderAPI[/B] — [ICODE]%tagify_prefix%[/ICODE], [ICODE]%tagify_suffix%[/ICODE], [ICODE]%tagify_group%[/ICODE]…
[/LIST]

[SIZE=5][B]❯ Premium edition[/B] ([URL=#]BuiltByBit[/URL])[/SIZE]

Everything above, plus:

[LIST]
[*]🗄️ [B]MySQL / MariaDB · PostgreSQL · MongoDB[/B] storage (HikariCP pool)
[*]🔄 [B]Real-time cross-server sync[/B] via Redis pub/sub — a tag change propagated across the whole network instantly
[*]📊 [B]Advanced tab-list sorting[/B] — explicit per-group weight, or an automatic mode
[*]🧮 [B]Conditional group priorities[/B] — per-world / per-permission gates, priority boosts
[*]🎛️ [B]Full GUI[/B] with weight control and colour / gradient presets
[*]🎧 [B]Priority support[/B]
[/LIST]

The Free edition stays [B]100% functional[/B] without the Premium module.

[HR][/HR]

[SIZE=5][B]❯ Compatibility[/B][/SIZE]

[LIST]
[*][B]Server[/B]: Paper 1.20.6 → 26.2+ (Purpur, Pufferfish, Folia). Spigot loads, Paper recommended.
[*][B]Java[/B]: 21+
[*][B]Optional[/B]: LuckPerms · PlaceholderAPI · Vault · ProtocolLib · ItemsAdder / Oraxen / Nexo
[/LIST]

[SIZE=5][B]❯ Quick install[/B][/SIZE]

[LIST=1]
[*]Drop the [ICODE].jar[/ICODE] into [ICODE]plugins/[/ICODE]
[*]Start the server, then edit [ICODE]config.yml[/ICODE] and [ICODE]lang/en.yml[/ICODE]
[*][ICODE]/tagify reload[/ICODE]
[/LIST]

Migration: keep NametagEdit installed, run [ICODE]/tagify import legacy[/ICODE], then remove it.

[HR][/HR]

[SIZE=5][B]❯ Tagify vs NametagEdit[/B][/SIZE]

[TABLE]
[TR][TD][B][/B][/TD][TD][B]NametagEdit[/B][/TD][TD][B]Tagify[/B][/TD][/TR]
[TR][TD]Last update[/TD][TD]stagnant[/TD][TD]active[/TD][/TR]
[TR][TD]ItemsAdder icons in prefix[/TD][TD]crash[/TD][TD]sanitised[/TD][/TR]
[TR][TD]Hex + gradients[/TD][TD]partial / buggy[/TD][TD]full MiniMessage[/TD][/TR]
[TR][TD]Folia[/TD][TD]no[/TD][TD]native[/TD][/TR]
[TR][TD]LuckPerms refresh[/TD][TD]polling / broken[/TD][TD]event-driven[/TD][/TR]
[TR][TD]Storage migration[/TD][TD]buggy[/TD][TD]reliable[/TD][/TR]
[TR][TD]API[/TD][TD]limited[/TD][TD]documented[/TD][/TR]
[/TABLE]

[SIZE=5][B]❯ Support[/B][/SIZE]

Free edition: [URL=https://github.com/Mathildeuh/Tagify/issues]GitHub Issues[/URL].
Premium edition: priority support via BuiltByBit.

[CENTER][SIZE=3]Free edition is open source (GPL-3.0) — code on GitHub.[/SIZE][/CENTER]
