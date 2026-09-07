package fr.mathildeuh.tagify.internal.command.sub;

import fr.mathildeuh.tagify.internal.BuildConstants;
import fr.mathildeuh.tagify.internal.command.CommandContext;
import fr.mathildeuh.tagify.internal.command.SubCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;

/** {@code /tagify version} — version, build, plateforme, backend, édition, intégrations. */
public final class VersionCommand implements SubCommand {

    @Override
    public String name() {
        return "version";
    }

    @Override
    public List<String> aliases() {
        return List.of("ver", "about");
    }

    @Override
    public String permission() {
        return "tagify.use";
    }

    @Override
    public String usageArgs() {
        return "";
    }

    @Override
    public String descriptionKey() {
        return "command.version.description";
    }

    @Override
    public void run(CommandContext ctx) {
        String channel = BuildConstants.SNAPSHOT ? "dev" : "stable";
        String edition = ctx.core().modules() != null && ctx.core().modules().isPremiumActive()
                ? "Premium" : "Free";
        List<String> integrations = ctx.core().integrations().activeIntegrations();

        ctx.msg("command.version.header",
                Placeholder.unparsed("version", BuildConstants.VERSION),
                Placeholder.unparsed("build", BuildConstants.BUILD_NUMBER),
                Placeholder.unparsed("branch", BuildConstants.BRANCH),
                Placeholder.unparsed("hash", BuildConstants.COMMIT),
                Placeholder.unparsed("channel", channel));
        ctx.msg("command.version.platform",
                Placeholder.unparsed("platform", ctx.core().platform().describe()));
        ctx.msg("command.version.storage",
                Placeholder.unparsed("backend", ctx.core().currentDataStore() == null
                        ? "-" : ctx.core().currentDataStore().id()));
        ctx.msg("command.version.edition", Placeholder.unparsed("edition", edition));
        ctx.msg("command.version.integrations",
                Placeholder.unparsed("integrations", integrations.isEmpty()
                        ? "-" : String.join(", ", integrations)));
    }
}
