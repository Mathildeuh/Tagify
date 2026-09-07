import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.0.0"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

// ---------------------------------------------------------------------------
//  Versioning dérivé de Git (aucune version codée en dur dans le code source)
//  - semver : dernier tag "vX.Y.Z"  (repli : baseVersion de gradle.properties)
//  - buildNumber : nombre de commits sur HEAD
//  - snapshot : true sauf si HEAD est exactement sur un tag "vX.Y.Z"
//  Les appels Git passent par providers.exec -> compatible configuration-cache.
// ---------------------------------------------------------------------------
fun gitValue(vararg args: String): Provider<String> =
    providers.exec {
        commandLine(listOf("git") + args)
        isIgnoreExitValue = true
    }.standardOutput.asText.map { it.trim() }

val semverRegex = Regex("""v\d+\.\d+\.\d+""")

val baseVersion = providers.gradleProperty("baseVersion").getOrElse("0.0.0")
val exactTag = gitValue("describe", "--tags", "--exact-match").getOrElse("")
val lastTag = gitValue("describe", "--tags", "--match", "v*", "--abbrev=0").getOrElse("")
val buildNumberProvider = gitValue("rev-list", "--count", "HEAD").map { it.ifEmpty { "0" } }
val commitHashProvider = gitValue("rev-parse", "--short=8", "HEAD").map { it.ifEmpty { "unknown" } }
val branchProvider = gitValue("rev-parse", "--abbrev-ref", "HEAD").map { it.ifEmpty { "unknown" } }

val isRelease = semverRegex.matches(exactTag)
val semver = when {
    isRelease -> exactTag.removePrefix("v")
    semverRegex.matches(lastTag) -> lastTag.removePrefix("v")
    else -> baseVersion
}
// No "-SNAPSHOT" suffix: dev vs. stable is already conveyed by the "+build.N" metadata and by
// BuildConstants.SNAPSHOT / the branch shown in /tagify version, without cluttering the version string.
val projectVersion = semver

group = providers.gradleProperty("group").getOrElse("fr.mathildeuh")
version = projectVersion

val paperApiVersion = "1.20"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.lucko.me/")
    maven("https://jitpack.io")
}

dependencies {
    // API du serveur : plancher 1.20.6 pour une compatibilité runtime 1.20.6 -> 26.2+
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")

    // Intégrations optionnelles (toutes en soft-depend / compileOnly)
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("org.jetbrains:annotations:26.0.2")

    // Seule dépendance embarquée dans le jar (relocalisée) : statistiques anonymes bStats.
    implementation("org.bstats:bstats-bukkit:3.2.1")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    // Aucune toolchain forcée (seul un JDK récent est requis pour compiler) ;
    // le bytecode cible Java 21 -> tourne sur tout serveur 1.20.6+.
}

// Version exacte pour les workflows CI : ./gradlew -q printVersion
tasks.register("printVersion") {
    notCompatibleWithConfigurationCache("Tâche de diagnostic uniquement")
    val resolved = projectVersion
    doLast { println(resolved) }
}

// ---------------------------------------------------------------------------
//  Génération de BuildConstants.java (version exacte, build, hash, branche)
// ---------------------------------------------------------------------------
val generatedSrcDir = layout.buildDirectory.dir("generated/sources/buildconstants/java/main")

val generateBuildConstants by tasks.registering {
    val outDir = generatedSrcDir
    val ver = providers.provider { projectVersion }
    val build = buildNumberProvider
    val hash = commitHashProvider
    val branch = branchProvider
    val snapshot = providers.provider { !isRelease }
    outputs.dir(outDir)
    outputs.upToDateWhen { false }
    doLast {
        val pkgDir = outDir.get().dir("fr/mathildeuh/tagify/internal").asFile
        pkgDir.mkdirs()
        val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        File(pkgDir, "BuildConstants.java").writeText(
            """
            package fr.mathildeuh.tagify.internal;

            /**
             * Constantes de build injectées automatiquement par Gradle depuis l'état Git.
             * NE PAS ÉDITER — fichier généré.
             */
            public final class BuildConstants {

                public static final String VERSION = "${ver.get()}";
                public static final String BUILD_NUMBER = "${build.get()}";
                public static final String COMMIT = "${hash.get()}";
                public static final String BRANCH = "${branch.get()}";
                public static final boolean SNAPSHOT = ${snapshot.get()};
                public static final String BUILD_TIME = "$now";

                /** Ex. {@code 1.4.2+build.87}. */
                public static String fullVersion() {
                    return VERSION + "+build." + BUILD_NUMBER;
                }

                private BuildConstants() {
                }
            }
            """.trimIndent() + "\n"
        )
    }
}

sourceSets {
    main {
        java.srcDir(generateBuildConstants)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:-options")
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val tokens = mapOf(
        "version" to projectVersion,
        "apiVersion" to paperApiVersion,
    )
    inputs.properties(tokens)
    filesMatching("plugin.yml") {
        expand(tokens)
    }
}

// Le jar distribué est le shadowJar (Core + bStats relocalisé). Le jar « thin » ne sert à rien.
tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveBaseName.set("Tagify")
    archiveClassifier.set("")
    archiveVersion.set("") // final name: Tagify.jar
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false

    // N'embarquer que bStats, rien d'autre.
    dependencies {
        exclude { it.moduleGroup != "org.bstats" }
    }
    relocate("org.bstats", "fr.mathildeuh.tagify.lib.bstats")
    mergeServiceFiles()

    manifest {
        attributes(
            "Implementation-Title" to "Tagify",
            "Implementation-Version" to projectVersion,
        )
    }
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

// Build everything in one command: Core jar + Premium jar (if the module is present locally).
val premiumPresent = findProject(":tagify-premium") != null
tasks.register("buildAll") {
    group = "build"
    description = "Builds the Core jar and, if present, the Premium jar."
    notCompatibleWithConfigurationCache("Aggregate convenience task")
    dependsOn(tasks.named("build"))
    if (premiumPresent) {
        dependsOn(":tagify-premium:shadowJar")
    }
    val libs = layout.buildDirectory.dir("libs").get().asFile.path
    doLast {
        logger.lifecycle("Core     -> $libs/Tagify.jar")
        if (premiumPresent) {
            logger.lifecycle("Premium  -> $libs/Tagify-Premium.jar")
        } else {
            logger.lifecycle("Premium  -> module not present (tagify-premium/) - Core only.")
        }
    }
}

val serverJvmArgs = listOf(
    "-Xms2G", "-Xmx2G",
    "-DPaper.IgnoreJavaVersion=true",
    "-Dfile.encoding=UTF-8",
    "-Dstdout.encoding=UTF-8",
    "-Dstderr.encoding=UTF-8",
)

// Test the Premium jar locally: ./gradlew runPremium  (needs verify-mode: dev/offline in config.yml)
if (premiumPresent) {
    tasks.register<xyz.jpenilla.runpaper.task.RunServer>("runPremium") {
        minecraftVersion("1.21.8")
        runDirectory.set(layout.projectDirectory.dir("run-premium"))
        pluginJars.setFrom(project(":tagify-premium").tasks.named("shadowJar").map { it.outputs.files.singleFile })
        jvmArgs(serverJvmArgs)
    }
}

tasks.runServer {
    minecraftVersion("1.21.8")
    jvmArgs(serverJvmArgs)
}
